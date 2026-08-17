package com.repartorouter.reparto_router_web.controller.dto;

public class RegistroRequest {
    private String email;
    private String password;
    private String nombre;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}