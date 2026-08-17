package com.repartorouter.reparto_router_web.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    // Clave secreta para firmar los tokens. En producción esto debería venir
    // de una variable de entorno, no estar escrito en el código.
    private static final String SECRET = "cambia-esta-clave-por-una-larga-y-aleatoria-de-al-menos-32-caracteres";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    private static final long EXPIRACION_MS = 1000L * 60 * 60 * 24 * 7; // 7 días

    public String generarToken(Long choferId, String email) {
        return Jwts.builder()
                .subject(email)
                .claim("choferId", choferId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRACION_MS))
                .signWith(KEY)
                .compact();
    }

    public Claims validarYObtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String obtenerEmail(String token) {
        return validarYObtenerClaims(token).getSubject();
    }

    public Long obtenerChoferId(String token) {
        return validarYObtenerClaims(token).get("choferId", Long.class);
    }

    public boolean esTokenValido(String token) {
        try {
            validarYObtenerClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}