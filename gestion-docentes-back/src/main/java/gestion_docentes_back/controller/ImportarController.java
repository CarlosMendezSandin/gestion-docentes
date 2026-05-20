package gestion_docentes_back.controller;

import gestion_docentes_back.model.Docente;
import gestion_docentes_back.model.Horario;
import gestion_docentes_back.repository.DocenteRepository;
import gestion_docentes_back.repository.HorarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/importar")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*",
        methods = {RequestMethod.POST, RequestMethod.OPTIONS})
public class ImportarController {

    @Autowired
    private DocenteRepository docenteRepository;

    @Autowired
    private HorarioRepository horarioRepository;

    /**
     * CSV esperado (con cabecera):
     * codigoProfesor,nombre,apellidos,email,departamento,tipoFuncionario,antiguedadCentro,notaOposicion,rol,password
     * Ejemplo:
     * DOC004,María,Pérez López,maria@laboral.com,Matemáticas,INTERINO,2,7.5,DOCENTE,1234
     */
    @PostMapping("/docentes")
    public ResponseEntity<?> importarDocentes(@RequestParam("file") MultipartFile file) {
        List<String> errores = new ArrayList<>();
        int importados = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String linea;
            boolean primera = true;
            int numLinea = 0;

            while ((linea = reader.readLine()) != null) {
                numLinea++;
                if (primera) { primera = false; continue; }
                if (linea.isBlank()) continue;

                String[] c = linea.split(",", -1);
                if (c.length < 6) {
                    errores.add("Línea " + numLinea + ": faltan columnas → " + linea);
                    continue;
                }

                try {
                    Docente d = new Docente();
                    d.setCodigoProfesor(c[0].trim());
                    d.setNombre(c[1].trim());
                    d.setApellidos(c[2].trim());
                    d.setEmail(c[3].trim());
                    d.setDepartamento(c[4].trim());
                    d.setTipoFuncionario(Docente.TipoFuncionario.valueOf(c[5].trim().toUpperCase()));
                    if (c.length > 6 && !c[6].isBlank()) d.setAntiguedadCentro(Integer.parseInt(c[6].trim()));
                    if (c.length > 7 && !c[7].isBlank()) d.setNotaOposicion(Double.parseDouble(c[7].trim()));
                    d.setRol(c.length > 8 && !c[8].isBlank()
                            ? Docente.Rol.valueOf(c[8].trim().toUpperCase())
                            : Docente.Rol.DOCENTE);
                    d.setPasswordHash(c.length > 9 && !c[9].isBlank() ? c[9].trim() : "1234");

                    String codigo = d.getCodigoProfesor();
                    docenteRepository.findByCodigoProfesor(codigo).ifPresentOrElse(existing -> {
                        existing.setNombre(d.getNombre());
                        existing.setApellidos(d.getApellidos());
                        existing.setEmail(d.getEmail());
                        existing.setDepartamento(d.getDepartamento());
                        existing.setTipoFuncionario(d.getTipoFuncionario());
                        existing.setAntiguedadCentro(d.getAntiguedadCentro());
                        existing.setNotaOposicion(d.getNotaOposicion());
                        docenteRepository.save(existing);
                    }, () -> docenteRepository.save(d));

                    importados++;
                } catch (Exception e) {
                    errores.add("Línea " + numLinea + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No se pudo leer el fichero: " + e.getMessage()));
        }

        return ResponseEntity.ok(Map.of("importados", importados, "errores", errores));
    }

    /**
     * CSV esperado (con cabecera):
     * codigoProfesor,diaSemana,tramoHorario,grupoAula
     * Ejemplo:
     * DOC001,LUNES,1,1DAW-A
     * diaSemana: LUNES | MARTES | MIERCOLES | JUEVES | VIERNES
     * tramoHorario: 1-6 (tramos del día)
     */
    @PostMapping("/horarios")
    public ResponseEntity<?> importarHorarios(@RequestParam("file") MultipartFile file) {
        List<String> errores = new ArrayList<>();
        int importados = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String linea;
            boolean primera = true;
            int numLinea = 0;

            while ((linea = reader.readLine()) != null) {
                numLinea++;
                if (primera) { primera = false; continue; }
                if (linea.isBlank()) continue;

                String[] c = linea.split(",", -1);
                if (c.length < 4) {
                    errores.add("Línea " + numLinea + ": faltan columnas → " + linea);
                    continue;
                }

                try {
                    String codigo = c[0].trim();
                    Horario.DiaSemana dia = Horario.DiaSemana.valueOf(c[1].trim().toUpperCase());
                    Integer tramo = Integer.parseInt(c[2].trim());
                    String grupoAula = c[3].trim();

                    Docente docente = docenteRepository.findByCodigoProfesor(codigo)
                            .orElseThrow(() -> new RuntimeException("Docente no encontrado: " + codigo));

                    Horario h = new Horario();
                    h.setDocente(docente);
                    h.setDiaSemana(dia);
                    h.setTramoHorario(tramo);
                    h.setGrupoAula(grupoAula);
                    horarioRepository.save(h);
                    importados++;
                } catch (Exception e) {
                    errores.add("Línea " + numLinea + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No se pudo leer el fichero: " + e.getMessage()));
        }

        return ResponseEntity.ok(Map.of("importados", importados, "errores", errores));
    }
}
