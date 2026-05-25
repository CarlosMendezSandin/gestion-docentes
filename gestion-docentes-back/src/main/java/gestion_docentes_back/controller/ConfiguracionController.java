package gestion_docentes_back.controller;

import gestion_docentes_back.service.ConfiguracionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionController {

    @Autowired
    private ConfiguracionService configuracionService;

    @GetMapping
    public ResponseEntity<?> getConfig() {
        return ResponseEntity.ok(Map.of(
                "maxAsuntosPropiosDia", configuracionService.getMaxAsuntosPropiosDia()
        ));
    }

    @PutMapping
    public ResponseEntity<?> setConfig(@RequestBody Map<String, Integer> body) {
        try {
            configuracionService.setMaxAsuntosPropiosDia(body.get("maxAsuntosPropiosDia"));
            return ResponseEntity.ok(Map.of(
                    "maxAsuntosPropiosDia", configuracionService.getMaxAsuntosPropiosDia()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
