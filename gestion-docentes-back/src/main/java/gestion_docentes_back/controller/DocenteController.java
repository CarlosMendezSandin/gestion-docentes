package gestion_docentes_back.controller;

import gestion_docentes_back.model.Docente;
import gestion_docentes_back.repository.DocenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/docentes")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", methods = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
        RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH
})
public class DocenteController {

    @Autowired
    private DocenteRepository docenteRepository;

    // GET /api/docentes — listar todos
    @GetMapping
    public List<Docente> listarTodos() {
        return docenteRepository.findAll();
    }

    // GET /api/docentes/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        return docenteRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/docentes/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String codigo = credentials.get("codigoProfesor");
        String password = credentials.get("passwordHash");

        if (codigo == null || password == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Faltan campos obligatorios");
        }

        return docenteRepository.findByCodigoProfesor(codigo)
                .map(docente -> {
                    if (password.equals(docente.getPasswordHash())) {
                        return ResponseEntity.ok((Object) docente);
                    }
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body((Object) "Contraseña incorrecta");
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado"));
    }

    // POST /api/docentes — crear docente (ADMIN)
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Docente docente) {
        try {
            Docente nuevo = docenteRepository.save(docente);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // PUT /api/docentes/{id} — editar docente (ADMIN)
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody Docente datos) {
        return docenteRepository.findById(id)
                .map(docente -> {
                    docente.setNombre(datos.getNombre());
                    docente.setApellidos(datos.getApellidos());
                    docente.setEmail(datos.getEmail());
                    docente.setDepartamento(datos.getDepartamento());
                    docente.setTipoFuncionario(datos.getTipoFuncionario());
                    docente.setRol(datos.getRol());
                    docente.setAntiguedadCentro(datos.getAntiguedadCentro());
                    docente.setNotaOposicion(datos.getNotaOposicion());
                    // Solo actualizar contraseña si se envía
                    if (datos.getPasswordHash() != null && !datos.getPasswordHash().isBlank()) {
                        docente.setPasswordHash(datos.getPasswordHash());
                    }
                    return ResponseEntity.ok(docenteRepository.save(docente));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/docentes/{id} — eliminar (ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        if (!docenteRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        docenteRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/docentes/{id}/cambiar-password — primer acceso o cambio voluntario
    @PutMapping("/{id}/cambiar-password")
    public ResponseEntity<?> cambiarPassword(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {
        String nuevaPassword = body.get("nuevaPassword");
        if (nuevaPassword == null || nuevaPassword.isBlank()) {
            return ResponseEntity.badRequest().body("La contraseña no puede estar vacía");
        }
        return docenteRepository.findById(id)
                .map(docente -> {
                    docente.setPasswordHash(nuevaPassword);
                    docente.setPrimerAcceso(0);
                    return ResponseEntity.ok(docenteRepository.save(docente));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
