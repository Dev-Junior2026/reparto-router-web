package com.repartorouter.reparto_router_web.repository;

import com.repartorouter.reparto_router_web.model.Chofer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ChoferRepository extends JpaRepository<Chofer, Long> {
    Optional<Chofer> findByEmail(String email);
    boolean existsByEmail(String email);
}