package com.unidad2.taller.controllers;

import com.unidad2.taller.entity.Averia;
import com.unidad2.taller.entity.Vehiculo;
import com.unidad2.taller.repositories.AveriaRepository;
import com.unidad2.taller.repositories.VehiculoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/averias")

public class AveriaController {

    private final AveriaRepository averiaRepository;
    private final VehiculoRepository vehiculoRepository;

    public AveriaController(AveriaRepository averiaRepository,
                            VehiculoRepository vehiculoRepository) {
        this.averiaRepository = averiaRepository;
        this.vehiculoRepository = vehiculoRepository;
    }

    // GET /api/averias -> 200
    @GetMapping
    public List<Averia> listarAverias() {
        return averiaRepository.findAll();
    }

    // GET /api/averias/{id} -> 200 o 404
    @GetMapping("/{id}")
    public ResponseEntity<Averia> obtenerAveria(@PathVariable Long id) {
        Optional<Averia> averia = averiaRepository.findById(id);
        return averia.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /api/averias -> 201, 400 o 404
    @PostMapping
    public ResponseEntity<Averia> crearAveria(@RequestBody Averia averia) {
        // 400: no se indica vehículo (ni vehículo, ni id)
        if (averia.getVehiculo() == null || averia.getVehiculo().getId() == null) {
            return ResponseEntity.badRequest().build();
        }

        // 404: el vehículo indicado no existe
        Optional<Vehiculo> vehiculoOpt =
                vehiculoRepository.findById(averia.getVehiculo().getId());
        if (vehiculoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Vehiculo vehiculo = vehiculoOpt.get();
        averia.setId(null);
        averia.setVehiculo(vehiculo);
        vehiculo.getAverias().add(averia);   // sincroniza el otro lado

        Averia guardada = averiaRepository.save(averia);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    // PUT /api/averias/{id} -> 200 o 404
    @PutMapping("/{id}")
    public ResponseEntity<Averia> actualizarAveria(@PathVariable Long id,
                                                   @RequestBody Averia datos) {
        return averiaRepository.findById(id)
                .map(averia -> {
                    averia.setDescripcion(datos.getDescripcion());
                    if (datos.getEstado() != null) {
                        averia.setEstado(datos.getEstado());
                    }
                    return ResponseEntity.ok(averiaRepository.save(averia));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE /api/averias/{id} -> 204 o 404
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarAveria(@PathVariable Long id) {
        return averiaRepository.findById(id)
                .map(averia -> {
                    averia.getVehiculo().getAverias().remove(averia);  // sincroniza
                    averiaRepository.delete(averia);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
