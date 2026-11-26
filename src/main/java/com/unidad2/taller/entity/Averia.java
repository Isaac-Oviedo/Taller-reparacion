package com.unidad2.taller.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor

public class Averia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReparacion estado = EstadoReparacion.PENDIENTE;

    @Column(updatable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaUltimaActualizacion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    @JsonIgnoreProperties("averias")
    private Vehiculo vehiculo;

    @PrePersist
    protected void alInsertar() {
        LocalDateTime ahora = LocalDateTime.now();
        this.fechaCreacion = ahora;
        this.fechaUltimaActualizacion = ahora;
        if (this.estado == null) {
            this.estado = EstadoReparacion.PENDIENTE;
        }
    }

    @PreUpdate
    protected void alActualizar() {
        this.fechaUltimaActualizacion = LocalDateTime.now();
    }
}
