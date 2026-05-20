package gestion_docentes_back;

import com.fasterxml.jackson.databind.ObjectMapper;
import gestion_docentes_back.model.Docente;
import gestion_docentes_back.repository.DocenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("UC8 - Modificar Datos de Docentes")
class UC8_ModificarDatosTest {

    @Autowired MockMvc mockMvc;
    @Autowired DocenteRepository docenteRepository;
    @Autowired ObjectMapper objectMapper;

    private Docente docente;

    @BeforeEach
    void setUp() {
        docente = docenteRepository.save(TestDataFactory.docente(
                "DOC001", "Carlos", "Méndez",
                Docente.TipoFuncionario.CARRERA, Docente.Rol.DOCENTE, 5, 8.0));
    }

    @Test
    @DisplayName("Listar todos los docentes devuelve la lista completa")
    void listarDocentes() throws Exception {
        docenteRepository.save(TestDataFactory.docente(
                "DOC002", "Ana", "García",
                Docente.TipoFuncionario.INTERINO, Docente.Rol.DOCENTE, 2, 6.5));

        mockMvc.perform(get("/api/docentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Obtener docente por ID devuelve sus datos")
    void obtenerDocentePorId() throws Exception {
        mockMvc.perform(get("/api/docentes/" + docente.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoProfesor").value("DOC001"))
                .andExpect(jsonPath("$.nombre").value("Carlos"));
    }

    @Test
    @DisplayName("Crear nuevo docente devuelve 201 con los datos guardados")
    void crearDocente() throws Exception {
        var nuevo = Map.of(
                "codigoProfesor", "DOC099",
                "nombre", "Nuevo",
                "apellidos", "Docente",
                "email", "nuevo@laboral.com",
                "departamento", "Ciencias",
                "tipoFuncionario", "INTERINO",
                "rol", "DOCENTE",
                "passwordHash", "1234",
                "primerAcceso", 1
        );

        mockMvc.perform(post("/api/docentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoProfesor").value("DOC099"));

        assertThat(docenteRepository.findByCodigoProfesor("DOC099")).isPresent();
    }

    @Test
    @DisplayName("Actualizar docente modifica sus campos correctamente")
    void actualizarDocente() throws Exception {
        var cambios = Map.of(
                "nombre", "Carlos Actualizado",
                "apellidos", "Méndez",
                "email", "actualizado@laboral.com",
                "departamento", "Nuevo Depto",
                "tipoFuncionario", "INTERINO",
                "rol", "DOCENTE",
                "antiguedadCentro", 10,
                "notaOposicion", 9.0,
                "passwordHash", ""
        );

        mockMvc.perform(put("/api/docentes/" + docente.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cambios)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Carlos Actualizado"))
                .andExpect(jsonPath("$.departamento").value("Nuevo Depto"))
                .andExpect(jsonPath("$.antiguedadCentro").value(10));
    }

    @Test
    @DisplayName("Eliminar docente lo borra de la BD")
    void eliminarDocente() throws Exception {
        mockMvc.perform(delete("/api/docentes/" + docente.getId()))
                .andExpect(status().isNoContent());

        assertThat(docenteRepository.findById(docente.getId())).isEmpty();
    }

    @Test
    @DisplayName("Obtener docente inexistente devuelve 404")
    void obtenerDocenteNoExistente() throws Exception {
        mockMvc.perform(get("/api/docentes/99999"))
                .andExpect(status().isNotFound());
    }
}
