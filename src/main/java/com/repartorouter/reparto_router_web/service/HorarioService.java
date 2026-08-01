package com.repartorouter.reparto_router_web.service;

import com.repartorouter.reparto_router_web.model.Parada;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class HorarioService {

    public boolean esAlcanzable(LocalTime horaLlegadaEstimada, Parada parada) {
        return !horaLlegadaEstimada.isAfter(parada.getHoraCierre());
    }

    public LocalTime calcularHoraLlegadaConEspera(LocalTime horaLlegadaEstimada, Parada parada) {
        if (horaLlegadaEstimada.isBefore(parada.getHoraApertura())) {
            return parada.getHoraApertura();
        }
        return horaLlegadaEstimada;
    }
}
