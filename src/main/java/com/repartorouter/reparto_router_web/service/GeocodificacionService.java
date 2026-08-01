package com.repartorouter.reparto_router_web.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class GeocodificacionService {

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://nominatim.openstreetmap.org")
            .defaultHeader("User-Agent", "RepartoRouterWeb/1.0 (uso educativo)")
            .build();

    /**
     * Geocodifica una dirección y devuelve [latitud, longitud].
     * Lanza RuntimeException si no se encuentra ninguna coincidencia.
     */
    public double[] geocodificar(String direccion) {
        List<Map<String, Object>> resultados = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", direccion)
                        .queryParam("format", "json")
                        .queryParam("limit", 1)
                        .build())
                .retrieve()
                .body(List.class);

        if (resultados == null || resultados.isEmpty()) {
            throw new RuntimeException("No se encontraron coordenadas para: " + direccion);
        }

        Map<String, Object> primerResultado = resultados.get(0);
        double lat = Double.parseDouble((String) primerResultado.get("lat"));
        double lon = Double.parseDouble((String) primerResultado.get("lon"));

        return new double[]{lat, lon};
    }
}
