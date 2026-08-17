package com.repartorouter.reparto_router_web.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rutas")
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numero ASC")
    @JsonManagedReference
    private List<Parada> paradasOrdenadas = new ArrayList<>();

    private double distanciaTotalKm;

    private Duration tiempoTotalEstimado;

    private LocalTime horaInicio;

    private LocalTime horaFinEstimada;

    private String nombre;

    @ManyToOne
    @JoinColumn(name = "chofer_id")
    private Chofer chofer;

    /**
     * Constructor vacío requerido por JPA/Hibernate.
     */
    protected Ruta() {
    }

    public Ruta(String nombre, LocalTime horaInicio) {
        this.nombre = nombre;
        this.horaInicio = horaInicio;
        this.paradasOrdenadas = new ArrayList<>();
        this.distanciaTotalKm = 0.0;
        this.tiempoTotalEstimado = Duration.ZERO;
        this.horaFinEstimada = null;
    }

    public Long getId() {
        return id;
    }

    public double getDistanciaTotalKm() {
        return distanciaTotalKm;
    }



    public void setDistanciaTotalKm(double distanciaTotalKm) {
        this.distanciaTotalKm = distanciaTotalKm;
    }

    public LocalTime getHoraFinEstimada() {
        return horaFinEstimada;
    }

    public void setHoraFinEstimada(LocalTime horaFinEstimada) {
        this.horaFinEstimada = horaFinEstimada;
    }



    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Parada> getParadasOrdenadas() {
        return paradasOrdenadas;
    }

    public void agregarParada(Parada parada) {
        parada.setRuta(this);
        this.paradasOrdenadas.add(parada);
    }

    public Duration getTiempoTotalEstimado() {
        return tiempoTotalEstimado;
    }

    public void setTiempoTotalEstimado(Duration tiempoTotalEstimado) {
        this.tiempoTotalEstimado = tiempoTotalEstimado;
    }

    public Chofer getChofer() {
        return chofer;
    }

    public void setChofer(Chofer chofer) {
        this.chofer = chofer;
    }
}