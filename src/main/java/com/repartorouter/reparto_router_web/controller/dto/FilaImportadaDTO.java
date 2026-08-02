package com.repartorouter.reparto_router_web.controller.dto;

import java.time.LocalTime;

public class FilaImportadaDTO {

    private String nombre;
    private String calle;
    private String codigoPostal;
    private String poblacion;
    private LocalTime horaApertura;
    private LocalTime horaCierre;
    private boolean horarioDetectado;

    public FilaImportadaDTO() {
    }

    public FilaImportadaDTO(String nombre, String calle, String codigoPostal, String poblacion,
                            LocalTime horaApertura, LocalTime horaCierre, boolean horarioDetectado) {
        this.nombre = nombre;
        this.calle = calle;
        this.codigoPostal = codigoPostal;
        this.poblacion = poblacion;
        this.horaApertura = horaApertura;
        this.horaCierre = horaCierre;
        this.horarioDetectado = horarioDetectado;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }

    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }

    public String getPoblacion() { return poblacion; }
    public void setPoblacion(String poblacion) { this.poblacion = poblacion; }

    public LocalTime getHoraApertura() { return horaApertura; }
    public void setHoraApertura(LocalTime horaApertura) { this.horaApertura = horaApertura; }

    public LocalTime getHoraCierre() { return horaCierre; }
    public void setHoraCierre(LocalTime horaCierre) { this.horaCierre = horaCierre; }

    public boolean isHorarioDetectado() { return horarioDetectado; }
    public void setHorarioDetectado(boolean horarioDetectado) { this.horarioDetectado = horarioDetectado; }
}
