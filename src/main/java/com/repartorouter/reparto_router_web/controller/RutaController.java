package com.repartorouter.reparto_router_web.controller;

import com.repartorouter.reparto_router_web.model.Ruta;
import com.repartorouter.reparto_router_web.repository.RutaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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