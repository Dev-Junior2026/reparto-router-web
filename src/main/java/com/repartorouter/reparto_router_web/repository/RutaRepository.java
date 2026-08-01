package com.repartorouter.reparto_router_web.repository;

import com.repartorouter.reparto_router_web.model.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Long> {
}
