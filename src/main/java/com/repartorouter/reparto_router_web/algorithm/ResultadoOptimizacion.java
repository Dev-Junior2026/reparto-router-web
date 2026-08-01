package com.repartorouter.reparto_router_web.algorithm;

import com.repartorouter.reparto_router_web.model.Parada;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

public class ResultadoOptimizacion {

    private final List<Parada> paradasEnOrden;
    private final double distanciaTotalKm;
    private final LocalTime horaFinEstimada;
    private final Duration tiempoTotalEstimado;

    public ResultadoOptimizacion(List<Parada> paradasEnOrden, double distanciaTotalKm,
                                 LocalTime horaFinEstimada, Duration tiempoTotalEstimado) {
        this.paradasEnOrden = paradasEnOrden;
        this.distanciaTotalKm = distanciaTotalKm;
        this.horaFinEstimada = horaFinEstimada;
        this.tiempoTotalEstimado = tiempoTotalEstimado;
    }

    public List<Parada> getParadasEnOrden() { return paradasEnOrden; }
    public double getDistanciaTotalKm() { return distanciaTotalKm; }
    public LocalTime getHoraFinEstimada() { return horaFinEstimada; }
    public Duration getTiempoTotalEstimado() { return tiempoTotalEstimado; }
}