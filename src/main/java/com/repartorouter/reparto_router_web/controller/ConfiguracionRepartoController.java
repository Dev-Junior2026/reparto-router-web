package com.repartorouter.reparto_router_web.controller;

import com.repartorouter.reparto_router_web.controller.dto.ConfiguracionRepartoRequest;
import com.repartorouter.reparto_router_web.model.ConfiguracionReparto;
import com.repartorouter.reparto_router_web.model.Parada;
import com.repartorouter.reparto_router_web.repository.ConfiguracionRepartoRepository;
import com.repartorouter.reparto_router_web.repository.ParadaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/configuraciones")
public class ConfiguracionRepartoController {

    @Autowired
    private ConfiguracionRepartoRepository configuracionRepository;

    @Autowired
    private ParadaRepository paradaRepository;

    // GET /api/configuraciones
    @GetMapping
    public List<ConfiguracionReparto> listar() {
        return configuracionRepository.findAll();
    }

    // GET /api/configuraciones/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ConfiguracionReparto> obtener(@PathVariable Long id) {
        return configuracionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/configuraciones
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody ConfiguracionRepartoRequest request) {
        Parada parada = paradaRepository.findById(request.getLugarCargaDescargaId())
                .orElse(null);

        if (parada == null) {
            return ResponseEntity.badRequest()
                    .body("No existe una Parada con id " + request.getLugarCargaDescargaId());
        }

        ConfiguracionReparto config = new ConfiguracionReparto(parada, request.getHoraInicioJornada());

        if (request.getTiempoDescargaDefecto() != null) {
            config.setTiempoDescargaDefecto(request.getTiempoDescargaDefecto());
        }
        if (request.getNavegadorPreferido() != null) {
            config.setNavegadorPreferido(request.getNavegadorPreferido());
        }

        ConfiguracionReparto guardada = configuracionRepository.save(config);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    // PUT /api/configuraciones/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody ConfiguracionRepartoRequest request) {
        return configuracionRepository.findById(id)
                .map(configExistente -> {
                    if (request.getLugarCargaDescargaId() != null) {
                        Parada parada = paradaRepository.findById(request.getLugarCargaDescargaId())
                                .orElse(null);
                        if (parada == null) {
                            return ResponseEntity.badRequest()
                                    .body("No existe una Parada con id " + request.getLugarCargaDescargaId());
                        }
                        configExistente.setLugarCargaDescarga(parada);
                    }
                    if (request.getHoraInicioJornada() != null) {
                        configExistente.setHoraInicioJornada(request.getHoraInicioJornada());
                    }
                    if (request.getTiempoDescargaDefecto() != null) {
                        configExistente.setTiempoDescargaDefecto(request.getTiempoDescargaDefecto());
                    }
                    if (request.getNavegadorPreferido() != null) {
                        configExistente.setNavegadorPreferido(request.getNavegadorPreferido());
                    }
                    ConfiguracionReparto actualizada = configuracionRepository.save(configExistente);
                    return ResponseEntity.ok(actualizada);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/configuraciones/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!configuracionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        configuracionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
