package gestion_docentes_back.controller;

import gestion_docentes_back.model.SolicitudAsuntosPropios;
import gestion_docentes_back.repository.SolicitudAsuntosPropiosRepository;
import gestion_docentes_back.service.AsuntosPropiosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/asuntos-propios")
public class AsuntosPropiosController {

    @Autowired
    private AsuntosPropiosService service;

    @Autowired
    private SolicitudAsuntosPropiosRepository repository;

    @Value("${app.upload-dir:uploads/material}")
    private String uploadDir;

    // Listar todas (ADMIN)
    @GetMapping
    public List<SolicitudAsuntosPropios> listarTodas() {
        return service.listarTodas();
    }

    // Listar por docente (PROFESOR ve las suyas)
    @GetMapping("/docente/{docenteId}")
    public List<SolicitudAsuntosPropios> listarPorDocente(@PathVariable Integer docenteId) {
        return service.listarPorDocente(docenteId);
    }

    // Crear nueva solicitud
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody SolicitudAsuntosPropios solicitud) {
        try {
            SolicitudAsuntosPropios nueva = service.crearSolicitud(solicitud);
            return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Cambiar estado (ADMIN aprueba o deniega)
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {
        try {
            SolicitudAsuntosPropios.EstadoSolicitud estado =
                    SolicitudAsuntosPropios.EstadoSolicitud.valueOf(body.get("estado"));
            SolicitudAsuntosPropios actualizada = service.actualizarEstado(id, estado);
            return ResponseEntity.ok(actualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // Procesar solicitud con algoritmo de prioridad
    @PostMapping("/{id}/procesar")
    public ResponseEntity<?> procesar(@PathVariable Integer id) {
        try {
            SolicitudAsuntosPropios s = service.listarTodas().stream()
                    .filter(x -> x.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
            return ResponseEntity.ok(service.procesarSolicitud(s));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Subir material para solicitud aprobada
    @PostMapping("/{id}/material")
    public ResponseEntity<?> subirMaterial(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file) {
        try {
            SolicitudAsuntosPropios s = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
            if (s.getEstado() != SolicitudAsuntosPropios.EstadoSolicitud.APROBADA) {
                return ResponseEntity.badRequest().body("Solo se puede subir material en solicitudes aprobadas");
            }
            Path dir = Paths.get(uploadDir, String.valueOf(id));
            Files.createDirectories(dir);
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return ResponseEntity.ok(Map.of("fichero", filename));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar el fichero: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Eliminar (ADMIN o el propio docente si esta PENDIENTE)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            service.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
