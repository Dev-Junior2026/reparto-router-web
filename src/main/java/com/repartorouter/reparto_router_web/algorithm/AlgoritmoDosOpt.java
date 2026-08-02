package com.repartorouter.reparto_router_web.algorithm;

import com.repartorouter.reparto_router_web.model.Parada;
import com.repartorouter.reparto_router_web.service.DistanciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class AlgoritmoDosOpt {

    @Autowired
    private DistanciaService distanciaService;

    @Autowired
    private HeuristicaVecino heuristicaVecino;

    /**
     * Mejora el orden de paradas usando 2-opt, minimizando la distancia total
     * (sin tener en cuenta horarios durante la optimización; los horarios se
     * recalculan después con HeuristicaVecino.recalcularConOrdenFijo).
     */
    public ResultadoOptimizacion mejorar(List<Parada> paradasEnOrden, LocalTime horaInicioJornada) {

        if (paradasEnOrden.size() <= 3) {
            // No hay suficientes paradas para que 2-opt aporte mejora
            return heuristicaVecino.recalcularConOrdenFijo(paradasEnOrden, horaInicioJornada);
        }

        Parada almacen = paradasEnOrden.get(0);

        // Copia de trabajo con el almacén duplicado al final SOLO para los cálculos de distancia.
        // Nunca se persiste esta duplicación.
        List<Parada> trabajo = new ArrayList<>(paradasEnOrden);
        trabajo.add(almacen);

        boolean mejora = true;
        int iteraciones = 0;
        int maxIteraciones = 100;

        while (mejora && iteraciones < maxIteraciones) {
            mejora = false;
            iteraciones++;

            for (int i = 1; i < trabajo.size() - 2; i++) {
                for (int k = i + 1; k < trabajo.size() - 1; k++) {
                    double distanciaActual = calcularDistanciaSegmento(trabajo, i - 1, i, k, k + 1);

                    invertirSegmento(trabajo, i, k);

                    double distanciaNueva = calcularDistanciaSegmento(trabajo, i - 1, i, k, k + 1);

                    if (distanciaNueva < distanciaActual) {
                        mejora = true;
                    } else {
                        invertirSegmento(trabajo, i, k);
                    }
                }
            }
        }

        // Quitar la duplicación del almacén antes de persistir
        List<Parada> ordenFinal = new ArrayList<>(trabajo.subList(0, trabajo.size() - 1));

        return heuristicaVecino.recalcularConOrdenFijo(ordenFinal, horaInicioJornada);
    }

    private double calcularDistanciaSegmento(List<Parada> paradas, int a, int b, int d, int e) {
        double dist = 0.0;

        if (a >= 0) {
            dist += distanciaService.calcularDistanciaKm(
                    paradas.get(a).getLatitud(), paradas.get(a).getLongitud(),
                    paradas.get(b).getLatitud(), paradas.get(b).getLongitud()
            );
        }

        dist += distanciaService.calcularDistanciaKm(
                paradas.get(d).getLatitud(), paradas.get(d).getLongitud(),
                paradas.get(e).getLatitud(), paradas.get(e).getLongitud()
        );

        return dist;
    }

    private void invertirSegmento(List<Parada> paradas, int i, int k) {
        while (i < k) {
            Parada temp = paradas.get(i);
            paradas.set(i, paradas.get(k));
            paradas.set(k, temp);
            i++;
            k--;
        }
    }
}
