package com.repartorouter.reparto_router_web.controller.dto;

public class EstadoParadaRequest {

    private boolean completada;

    public EstadoParadaRequest() {
    }

    public boolean isCompletada() {
        return completada;
    }

    public void setCompletada(boolean completada) {
        this.completada = completada;
    }
}