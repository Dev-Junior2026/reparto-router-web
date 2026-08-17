package com.repartorouter.reparto_router_web.config;

import com.repartorouter.reparto_router_web.model.Chofer;
import com.repartorouter.reparto_router_web.model.Ruta;
import com.repartorouter.reparto_router_web.repository.ChoferRepository;
import com.repartorouter.reparto_router_web.repository.RutaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Se ejecuta una vez en cada arranque del backend. Para cada Ruta que todavía
 * no tenga un Chofer enlazado, busca (o crea) un Chofer con ese mismo nombre
 * y los enlaza. Los choferes creados automáticamente reciben un email y
 * contraseña provisionales que habrá que cambiar antes de producción real.
 */
@Component
public class MigracionChoferesRunner implements CommandLineRunner {

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private ChoferRepository choferRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        rutaRepository.findAll().forEach(ruta -> {
            if (ruta.getChofer() != null) {
                return;
            }

            String nombreChofer = ruta.getNombre();
            if (nombreChofer == null || nombreChofer.isBlank()) {
                return;
            }

            Chofer chofer = choferRepository.findAll().stream()
                    .filter(c -> nombreChofer.equals(c.getNombre()))
                    .findFirst()
                    .orElseGet(() -> {
                        String emailProvisional = nombreChofer.toLowerCase() + "@repartorouter.local";
                        String passwordProvisional = passwordEncoder.encode(UUID.randomUUID().toString());
                        Chofer nuevo = new Chofer(emailProvisional, passwordProvisional, nombreChofer);
                        return choferRepository.save(nuevo);
                    });

            ruta.setChofer(chofer);
            rutaRepository.save(ruta);
        });
    }
}