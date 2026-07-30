package com.repartorouter.reparto_router_web.repository;

import com.repartorouter.reparto_router_web.model.Parada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParadaRepository extends JpaRepository<Parada, Long> {
}
