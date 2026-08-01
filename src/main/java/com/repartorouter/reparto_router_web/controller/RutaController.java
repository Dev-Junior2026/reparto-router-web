package com.repartorouter.reparto_router_web.controller;

import com.repartorouter.reparto_router_web.model.Ruta;
import com.repartorouter.reparto_router_web.repository.RutaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.repartorouter.reparto_router_web.controller.dto.ParadaRequest;
import com.repartorouter.reparto_router_web.model.Parada;
import com.repartorouter.reparto_router_web.repository.ParadaRepository;

import java.util.List;

@RestController
@RequestMapping("/api/rutas")
public class RutaController {

    @Autowired
    private RutaRepository rutaRepository;

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

    @Autowired
    private ParadaRepository paradaRepository;
    // POST /api/rutas/{rutaId}/paradas  (crea una parada y la asocia a la ruta)
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

                    ruta.agregarParada(parada); // ya existe en tu entidad Ruta, asocia parada.setRuta(this)
                    paradaRepository.save(parada);

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
}