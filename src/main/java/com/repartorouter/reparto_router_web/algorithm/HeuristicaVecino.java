package com.repartorouter.reparto_router_web.algorithm;

import com.repartorouter.reparto_router_web.model.Parada;
import com.repartorouter.reparto_router_web.service.DistanciaService;
import com.repartorouter.reparto_router_web.service.HorarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class HeuristicaVecino {

    private static final double VELOCIDAD_MEDIA_KMH = 40.0;

    @Autowired
    private DistanciaService distanciaService;

    @Autowired
    private HorarioService horarioService;

    /**
     * Calcula el orden óptimo de visita de las paradas, empezando y terminando en el almacén.
     * A diferencia del desktop, el almacén NO se duplica como Parada: su vuelta se refleja
     * solo en distanciaTotalKm y horaFinEstimada.
     */
    public ResultadoOptimizacion calcular(List<Parada> paradas, LocalTime horaInicioJornada) {

        Parada almacen = null;
        for (Parada parada : paradas) {
            if (parada.isEsAlmacen()) {
                almacen = parada;
            }
        }

        if (almacen == null) {
            throw new IllegalStateException("La ruta no tiene ninguna parada marcada como almacén");
        }

        List<Parada> pendientes = new ArrayList<>(paradas);
        pendientes.remove(almacen);

        List<Parada> paradasEnOrden = new ArrayList<>();
        almacen.setNumero(1);
        almacen.setHoraLlegadaEstimada(horaInicioJornada);
        paradasEnOrden.add(almacen);

        LocalTime horaActual = horaInicioJornada;
        Parada posicionActual = almacen;
        double distanciaTotalKm = 0.0;
        int siguienteNumero = 2;

        while (!pendientes.isEmpty()) {

            Parada mejorParada = null;
            double mejorDistancia = Double.MAX_VALUE;
            LocalTime mejorHoraLlegada = null;

            for (Parada candidata : pendientes) {
                double distancia = distanciaService.calcularDistanciaKm(
                        posicionActual.getLatitud(), posicionActual.getLongitud(),
                        candidata.getLatitud(), candidata.getLongitud()
                );

                double tiempoViajeMinutos = (distancia / VELOCIDAD_MEDIA_KMH) * 60;
                LocalTime horaLlegadaCandidata = horaActual.plusMinutes((long) tiempoViajeMinutos);

                if (horarioService.esAlcanzable(horaLlegadaCandidata, candidata)) {
                    if (distancia < mejorDistancia) {
                        mejorDistancia = distancia;
                        mejorParada = candidata;
                        mejorHoraLlegada = horaLlegadaCandidata;
                    }
                }
            }

            // Si ninguna parada es alcanzable dentro de horario, se elige la más cercana igualmente
            if (mejorParada == null) {
                for (Parada candidata : pendientes) {
                    double distancia = distanciaService.calcularDistanciaKm(
                            posicionActual.getLatitud(), posicionActual.getLongitud(),
                            candidata.getLatitud(), candidata.getLongitud()
                    );

                    double tiempoViajeMinutos = (distancia / VELOCIDAD_MEDIA_KMH) * 60;
                    LocalTime horaLlegadaCandidata = horaActual.plusMinutes((long) tiempoViajeMinutos);

                    if (distancia < mejorDistancia) {
                        mejorDistancia = distancia;
                        mejorParada = candidata;
                        mejorHoraLlegada = horaLlegadaCandidata;
                    }
                }
            }

            horaActual = horarioService.calcularHoraLlegadaConEspera(mejorHoraLlegada, mejorParada);
            horaActual = horaActual.plusMinutes(mejorParada.getTiempoDescargaMin());

            mejorParada.setHoraLlegadaEstimada(horaActual);
            mejorParada.setNumero(siguienteNumero++);

            paradasEnOrden.add(mejorParada);
            distanciaTotalKm += mejorDistancia;
            pendientes.remove(mejorParada);
            posicionActual = mejorParada;
        }

        // Vuelta al almacén (solo afecta a los totales, no crea una Parada nueva)
        double distanciaVuelta = distanciaService.calcularDistanciaKm(
                posicionActual.getLatitud(), posicionActual.getLongitud(),
                almacen.getLatitud(), almacen.getLongitud()
        );

        double tiempoViajeVueltaMinutos = (distanciaVuelta / VELOCIDAD_MEDIA_KMH) * 60;
        horaActual = horaActual.plusMinutes((long) tiempoViajeVueltaMinutos);
        distanciaTotalKm += distanciaVuelta;

        Duration tiempoTotal = Duration.between(horaInicioJornada, horaActual);

        return new ResultadoOptimizacion(paradasEnOrden, distanciaTotalKm, horaActual, tiempoTotal);
    }

    /**
     * Dado un orden fijo de paradas (empezando por el almacén), recalcula
     * horaLlegadaEstimada, numero, y los totales de la ruta.
     * No decide el orden — solo "simula" recorrerlo tal cual se le pasa.
     */
    public ResultadoOptimizacion recalcularConOrdenFijo(List<Parada> ordenFijo, LocalTime horaInicioJornada) {
        Parada almacen = ordenFijo.get(0);

        LocalTime horaActual = horaInicioJornada;
        Parada posicionActual = almacen;
        double distanciaTotalKm = 0.0;

        almacen.setNumero(1);
        almacen.setHoraLlegadaEstimada(horaInicioJornada);

        for (int i = 1; i < ordenFijo.size(); i++) {
            Parada siguiente = ordenFijo.get(i);

            double distancia = distanciaService.calcularDistanciaKm(
                    posicionActual.getLatitud(), posicionActual.getLongitud(),
                    siguiente.getLatitud(), siguiente.getLongitud()
            );

            double tiempoViajeMinutos = (distancia / VELOCIDAD_MEDIA_KMH) * 60;
            LocalTime horaLlegada = horaActual.plusMinutes((long) tiempoViajeMinutos);

            horaActual = horarioService.calcularHoraLlegadaConEspera(horaLlegada, siguiente);
            horaActual = horaActual.plusMinutes(siguiente.getTiempoDescargaMin());

            siguiente.setHoraLlegadaEstimada(horaActual);
            siguiente.setNumero(i + 1);

            distanciaTotalKm += distancia;
            posicionActual = siguiente;
        }

        // Vuelta al almacén
        double distanciaVuelta = distanciaService.calcularDistanciaKm(
                posicionActual.getLatitud(), posicionActual.getLongitud(),
                almacen.getLatitud(), almacen.getLongitud()
        );
        double tiempoVueltaMinutos = (distanciaVuelta / VELOCIDAD_MEDIA_KMH) * 60;
        horaActual = horaActual.plusMinutes((long) tiempoVueltaMinutos);
        distanciaTotalKm += distanciaVuelta;

        Duration tiempoTotal = Duration.between(horaInicioJornada, horaActual);

        return new ResultadoOptimizacion(ordenFijo, distanciaTotalKm, horaActual, tiempoTotal);
    }

}
