package com.repartorouter.reparto_router_web.controller;

import com.repartorouter.reparto_router_web.algorithm.AlgoritmoDosOpt;
import com.repartorouter.reparto_router_web.algorithm.HeuristicaVecino;
import com.repartorouter.reparto_router_web.algorithm.ResultadoOptimizacion;
import com.repartorouter.reparto_router_web.controller.dto.FilaImportadaDTO;
import com.repartorouter.reparto_router_web.controller.dto.ParadaRequest;
import com.repartorouter.reparto_router_web.model.Parada;
import com.repartorouter.reparto_router_web.model.Ruta;
import com.repartorouter.reparto_router_web.repository.ParadaRepository;
import com.repartorouter.reparto_router_web.repository.RutaRepository;
import com.repartorouter.reparto_router_web.service.GeocodificacionService;
import com.repartorouter.reparto_router_web.service.ImportadorPdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/rutas")
public class RutaController {

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private ParadaRepository paradaRepository;

    @Autowired
    private GeocodificacionService geocodificacionService;

    @Autowired
    private HeuristicaVecino heuristicaVecino;

    @Autowired
    private AlgoritmoDosOpt algoritmoDosOpt; // actualmente sin usar en /optimizar, ver nota abajo

    @Autowired
    private ImportadorPdfService importadorPdfService;

    // GET /api/rutas
    @GetMapping
    public List<Ruta> listarRutas() {
        return rutaRepository.findAll();
    }

    // GET /api/rutas/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Ruta> obtenerRuta(@PathVariable Long id) {
        return rutaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/rutas  (crea ruta vacía, sin paradas)
    @PostMapping
    public ResponseEntity<Ruta> crearRuta(@RequestBody Ruta ruta) {
        Ruta guardada = rutaRepository.save(ruta);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    // POST /api/rutas/{rutaId}/paradas  (crea una parada, la geocodifica y la asocia a la ruta)
    @PostMapping("/{rutaId}/paradas")
    public ResponseEntity<?> agregarParada(@PathVariable Long rutaId, @RequestBody ParadaRequest request) {
        return rutaRepository.findById(rutaId)
                .map(ruta -> {
                    int siguienteNumero = ruta.getParadasOrdenadas().size() + 1;

                    Parada parada = new Parada(
                            siguienteNumero,
                            request.getNombre(),
                            request.getCalle(),
                            request.getCodigoPostal(),
                            request.getPoblacion(),
                            request.getHoraApertura(),
                            request.getHoraCierre()
                    );

                    try {
                        double[] coords = geocodificacionService.geocodificar(parada.getDireccion());
                        parada.setLatitud(coords[0]);
                        parada.setLongitud(coords[1]);
                    } catch (RuntimeException e) {
                        // Se guarda igualmente con lat/lon en 0.0 si falla la geocodificación
                    }

                    ruta.agregarParada(parada);
                    paradaRepository.save(parada);

                    recalcularTotalesRuta(ruta);

                    return ResponseEntity.status(HttpStatus.CREATED).body(parada);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/rutas/{rutaId}/paradas  (lista las paradas de esa ruta, ya ordenadas)
    @GetMapping("/{rutaId}/paradas")
    public ResponseEntity<?> listarParadas(@PathVariable Long rutaId) {
        return rutaRepository.findById(rutaId)
                .map(ruta -> ResponseEntity.ok(ruta.getParadasOrdenadas()))
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/rutas/{rutaId}/paradas/{paradaId}
    @DeleteMapping("/{rutaId}/paradas/{paradaId}")
    public ResponseEntity<?> eliminarParada(@PathVariable Long rutaId, @PathVariable Long paradaId) {
        return rutaRepository.findById(rutaId)
                .map(ruta -> {
                    Parada parada = paradaRepository.findById(paradaId).orElse(null);

                    if (parada == null || !parada.getRuta().getId().equals(rutaId)) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("La parada no existe o no pertenece a esta ruta");
                    }

                    ruta.getParadasOrdenadas().remove(parada);
                    paradaRepository.delete(parada);

                    recalcularTotalesRuta(ruta);

                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/rutas/{id}  (editar horaInicio, horaFinEstimada, etc.)
    @PutMapping("/{id}")
    public ResponseEntity<Ruta> actualizarRuta(@PathVariable Long id, @RequestBody Ruta datosRuta) {
        return rutaRepository.findById(id)
                .map(rutaExistente -> {
                    rutaExistente.setHoraInicio(datosRuta.getHoraInicio());
                    rutaExistente.setHoraFinEstimada(datosRuta.getHoraFinEstimada());
                    rutaExistente.setDistanciaTotalKm(datosRuta.getDistanciaTotalKm());
                    rutaExistente.setTiempoTotalEstimado(datosRuta.getTiempoTotalEstimado());
                    Ruta actualizada = rutaRepository.save(rutaExistente);
                    return ResponseEntity.ok(actualizada);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/rutas/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRuta(@PathVariable Long id) {
        if (!rutaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        rutaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/rutas/{id}/optimizar
    @PostMapping("/{id}/optimizar")
    public ResponseEntity<?> optimizarRuta(@PathVariable Long id) {
        return rutaRepository.findById(id)
                .map(ruta -> {
                    List<Parada> paradas = ruta.getParadasOrdenadas();

                    if (paradas.isEmpty()) {
                        return ResponseEntity.badRequest().body("La ruta no tiene paradas que optimizar");
                    }

                    boolean tieneAlmacen = paradas.stream().anyMatch(Parada::isEsAlmacen);
                    if (!tieneAlmacen) {
                        return ResponseEntity.badRequest().body("La ruta no tiene ninguna parada marcada como almacén");
                    }

                    boolean todasGeocodificadas = paradas.stream()
                            .allMatch(p -> p.getLatitud() != 0 || p.getLongitud() != 0);
                    if (!todasGeocodificadas) {
                        return ResponseEntity.badRequest().body("Hay paradas sin coordenadas; geocodifícalas antes de optimizar");
                    }

                    // 2-Opt desactivado intencionalmente: el criterio de negocio prioriza
                    // hora de apertura sobre distancia, y el 2-Opt (puramente distancia)
                    // podría deshacer ese orden. Se deja HeuristicaVecino como único paso.
                    ResultadoOptimizacion resultado = heuristicaVecino.calcular(paradas, ruta.getHoraInicio());

                    for (Parada parada : resultado.getParadasEnOrden()) {
                        paradaRepository.save(parada);
                    }

                    ruta.setDistanciaTotalKm(resultado.getDistanciaTotalKm());
                    ruta.setHoraFinEstimada(resultado.getHoraFinEstimada());
                    ruta.setTiempoTotalEstimado(resultado.getTiempoTotalEstimado());
                    Ruta rutaActualizada = rutaRepository.save(ruta);

                    return ResponseEntity.ok(rutaActualizada);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/rutas/{rutaId}/importar-pdf  (extrae filas del PDF, sin guardar)
    @PostMapping("/{rutaId}/importar-pdf")
    public ResponseEntity<?> importarPdf(@PathVariable Long rutaId, @RequestParam("archivo") MultipartFile archivo) {

        if (!rutaRepository.existsById(rutaId)) {
            return ResponseEntity.notFound().build();
        }

        if (archivo.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo está vacío");
        }

        try {
            List<FilaImportadaDTO> filas = importadorPdfService.extraerFilas(archivo.getInputStream());

            if (filas.isEmpty()) {
                return ResponseEntity.badRequest().body("No se encontraron filas válidas en el PDF");
            }

            return ResponseEntity.ok(filas);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error al leer el PDF: " + e.getMessage());
        }
    }

    // POST /api/rutas/{rutaId}/confirmar-importacion  (guarda las filas ya editadas por el usuario)
    @PostMapping("/{rutaId}/confirmar-importacion")
    public ResponseEntity<?> confirmarImportacion(@PathVariable Long rutaId, @RequestBody List<FilaImportadaDTO> filas) {

        return rutaRepository.findById(rutaId)
                .map(ruta -> {

                    if (filas.isEmpty()) {
                        return ResponseEntity.badRequest().body("No hay filas para importar");
                    }

                    int siguienteNumero = ruta.getParadasOrdenadas().size() + 1;
                    List<Parada> paradasCreadas = new ArrayList<>();

                    for (FilaImportadaDTO fila : filas) {
                        Parada parada = new Parada(
                                siguienteNumero++,
                                fila.getNombre(),
                                fila.getCalle(),
                                fila.getCodigoPostal(),
                                fila.getPoblacion(),
                                fila.getHoraApertura(),
                                fila.getHoraCierre()
                        );

                        try {
                            double[] coords = geocodificacionService.geocodificar(parada.getDireccion());
                            parada.setLatitud(coords[0]);
                            parada.setLongitud(coords[1]);
                        } catch (RuntimeException e) {
                            // Se guarda igualmente con lat/lon en 0.0 si falla la geocodificación
                        }

                        try {
                            Thread.sleep(1100); // respeta el límite de 1 req/seg de Nominatim
                        } catch (InterruptedException ignored) {
                            Thread.currentThread().interrupt();
                        }

                        ruta.agregarParada(parada);
                        paradaRepository.save(parada);
                        paradasCreadas.add(parada);
                    }

                    recalcularTotalesRuta(ruta);

                    return ResponseEntity.status(HttpStatus.CREATED).body(paradasCreadas);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Recalcula numero (renumeración secuencial), distanciaTotalKm, horaFinEstimada
     * y tiempoTotalEstimado de una ruta, basándose en el orden ACTUAL de sus paradas
     * (no reordena, solo "simula" recorrer la ruta tal cual está para corregir
     * numeración y totales). Si alguna parada no está geocodificada, no recalcula,
     * para evitar guardar una distancia falsa calculada hacia el punto [0,0].
     */
    private void recalcularTotalesRuta(Ruta ruta) {
        List<Parada> paradas = ruta.getParadasOrdenadas();
        if (paradas.isEmpty()) return;

        boolean todasGeocodificadas = paradas.stream()
                .allMatch(p -> p.getLatitud() != 0 || p.getLongitud() != 0);
        if (!todasGeocodificadas) return;

        List<Parada> ordenActual = new ArrayList<>(paradas); // ya viene ordenado por numero (@OrderBy)
        ResultadoOptimizacion resultado = heuristicaVecino.recalcularConOrdenFijo(ordenActual, ruta.getHoraInicio());

        for (Parada p : resultado.getParadasEnOrden()) {
            paradaRepository.save(p);
        }

        ruta.setDistanciaTotalKm(resultado.getDistanciaTotalKm());
        ruta.setHoraFinEstimada(resultado.getHoraFinEstimada());
        ruta.setTiempoTotalEstimado(resultado.getTiempoTotalEstimado());
        rutaRepository.save(ruta);
    }
}