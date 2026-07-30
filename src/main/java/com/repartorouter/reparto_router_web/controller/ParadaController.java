package com.repartorouter.reparto_router_web.controller;

import com.repartorouter.reparto_router_web.model.Parada;
import com.repartorouter.reparto_router_web.repository.ParadaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paradas")
public class ParadaController {

    private final ParadaRepository paradaRepository;

    @Autowired
    public ParadaController(ParadaRepository paradaRepository) {
        this.paradaRepository = paradaRepository;
    }

    @GetMapping
    public List<Parada> listarTodas() {
        return paradaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Parada> obtenerPorId(@PathVariable Long id) {
        return paradaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Parada crear(@RequestBody Parada parada) {
        return paradaRepository.save(parada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!paradaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        paradaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}