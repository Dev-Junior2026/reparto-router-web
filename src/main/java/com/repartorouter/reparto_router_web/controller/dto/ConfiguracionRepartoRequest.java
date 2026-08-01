package com.repartorouter.reparto_router_web.controller.dto;

import java.time.LocalTime;

public class ConfiguracionRepartoRequest {

    private Long lugarCargaDescargaId;
    private LocalTime horaInicioJornada;
    private Integer tiempoDescargaDefecto;
    private String navegadorPreferido;

    public Long getLugarCargaDescargaId() {
        return lugarCargaDescargaId;
    }

    public void setLugarCargaDescargaId(Long lugarCargaDescargaId) {
        this.lugarCargaDescargaId = lugarCargaDescargaId;
    }

    public LocalTime getHoraInicioJornada() {
        return horaInicioJornada;
    }

    public void setHoraInicioJornada(LocalTime horaInicioJornada) {
        this.horaInicioJornada = horaInicioJornada;
    }

    public Integer getTiempoDescargaDefecto() {
        return tiempoDescargaDefecto;
    }

    public void setTiempoDescargaDefecto(Integer tiempoDescargaDefecto) {
        this.tiempoDescargaDefecto = tiempoDescargaDefecto;
    }

    public String getNavegadorPreferido() {
        return navegadorPreferido;
    }

    public void setNavegadorPreferido(String navegadorPreferido) {
        this.navegadorPreferido = navegadorPreferido;
    }
}