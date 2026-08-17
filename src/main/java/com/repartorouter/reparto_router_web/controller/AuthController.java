package com.repartorouter.reparto_router_web.controller;

import com.repartorouter.reparto_router_web.controller.dto.LoginRequest;
import com.repartorouter.reparto_router_web.controller.dto.RegistroRequest;
import com.repartorouter.reparto_router_web.model.Chofer;
import com.repartorouter.reparto_router_web.repository.ChoferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private ChoferRepository choferRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody RegistroRequest request) {
        if (choferRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Ese email ya está registrado");
        }

        String hash = passwordEncoder.encode(request.getPassword());
        Chofer chofer = new Chofer(request.getEmail(), hash, request.getNombre());
        Chofer guardado = choferRepository.save(chofer);

        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return choferRepository.findByEmail(request.getEmail())
                .filter(chofer -> passwordEncoder.matches(request.getPassword(), chofer.getPasswordHash()))
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email o contraseña incorrectos"));
    }
}