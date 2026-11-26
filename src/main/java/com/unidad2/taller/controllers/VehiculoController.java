package com.unidad2.taller.controllers;

import com.unidad2.taller.entity.Averia;
import com.unidad2.taller.entity.EstadoReparacion;
import com.unidad2.taller.entity.Vehiculo;
import com.unidad2.taller.repositories.VehiculoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vehiculos")

public class VehiculoController {

    private final VehiculoRepository vehiculoRepository;

    public VehiculoController(VehiculoRepository vehiculoRepository) {
        this.vehiculoRepository = vehiculoRepository;
    }


    @GetMapping
    public List<Vehiculo> listarVehiculos() {
        return vehiculoRepository.findAll();
    }


    @GetMapping("/{id}")
    public ResponseEntity<Vehiculo> obtenerVehiculo(@PathVariable Long id) {
        Optional<Vehiculo> vehiculo = vehiculoRepository.findById(id);
        return vehiculo.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /api/vehiculos -> 201
    @PostMapping
    public ResponseEntity<Vehiculo> crearVehiculo(@RequestBody Vehiculo vehiculo) {
        vehiculo.setId(null);
        if (vehiculo.getAverias() == null) {
            vehiculo.setAverias(new ArrayList<>());
        }
        Vehiculo nuevo = vehiculoRepository.save(vehiculo);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    // PUT /api/vehiculos/{id} -> 200 o 404
    @PutMapping("/{id}")
    public ResponseEntity<Vehiculo> actualizarVehiculo(@PathVariable Long id,
                                                       @RequestBody Vehiculo datos) {
        return vehiculoRepository.findById(id)
                .map(vehiculo -> {
                    vehiculo.setMatricula(datos.getMatricula());
                    vehiculo.setModelo(datos.getModelo());
                    vehiculo.setFechaMatriculacion(datos.getFechaMatriculacion());
                    return ResponseEntity.ok(vehiculoRepository.save(vehiculo));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET /api/vehiculos/{id}/averias -> 200 o 404
    @GetMapping("/{id}/averias")
    public ResponseEntity<List<Averia>> listarAveriasDeVehiculo(@PathVariable Long id) {
        return vehiculoRepository.findById(id)
                .map(vehiculo -> ResponseEntity.ok(vehiculo.getAverias()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE /api/vehiculos/{id} -> 204, 404 o 409
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarVehiculo(@PathVariable Long id) {
        Optional<Vehiculo> vehiculoOpt = vehiculoRepository.findById(id);
        if (vehiculoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();                    // 404
        }

        Vehiculo vehiculo = vehiculoOpt.get();

        boolean hayAveriasSinTerminar = vehiculo.getAverias().stream()
                .anyMatch(a -> a.getEstado() != EstadoReparacion.TERMINADO);

        if (hayAveriasSinTerminar) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();   // 409
        }

        vehiculoRepository.delete(vehiculo);   // CascadeType.REMOVE borra sus averías
        return ResponseEntity.noContent().build();                       // 204
    }
}
