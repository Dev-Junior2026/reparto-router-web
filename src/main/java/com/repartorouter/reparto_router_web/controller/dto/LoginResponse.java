package com.repartorouter.reparto_router_web.controller.dto;

public class LoginResponse {
    private String token;
    private Long choferId;
    private String nombre;
    private String email;

    public LoginResponse(String token, Long choferId, String nombre, String email) {
        this.token = token;
        this.choferId = choferId;
        this.nombre = nombre;
        this.email = email;
    }

    public String getToken() { return token; }
    public Long getChoferId() { return choferId; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
}