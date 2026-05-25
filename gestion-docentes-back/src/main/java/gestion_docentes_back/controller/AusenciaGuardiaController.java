package gestion_docentes_back.controller;

import gestion_docentes_back.model.Ausencia;
import gestion_docentes_back.model.Guardia;
import gestion_docentes_back.service.GuardiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// AUSENCIAS
@RestController
@RequestMapping("/api/ausencias")
class AusenciaController {

    @Autowired
    private GuardiaService guardiaService;

    @GetMapping
    public List<Ausencia> listarTodas() {
        return guardiaService.listarAusencias();
    }

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody Ausencia ausencia) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(guardiaService.registrarAusencia(ausencia));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

// GUARDIAS
@RestController
@RequestMapping("/api/guardias")
class GuardiaController {

    @Autowired
    private GuardiaService guardiaService;

    @GetMapping
    public List<Guardia> listarTodas() {
        return guardiaService.listarTodas();
    }

    @GetMapping("/fecha/{fecha}")
    public List<Guardia> listarPorFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return guardiaService.listarPorFecha(fecha);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {
        try {
            Guardia.EstadoGuardia estado = Guardia.EstadoGuardia.valueOf(body.get("estado"));
            String observaciones = body.get("observaciones");
            return ResponseEntity.ok(guardiaService.actualizarEstadoGuardia(id, estado, observaciones));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
