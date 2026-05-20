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
@DisplayName("UC7 - Primer acceso y cambio de contraseña")
class UC7_CambiarPasswordTest {

    @Autowired MockMvc mockMvc;
    @Autowired DocenteRepository docenteRepository;
    @Autowired ObjectMapper objectMapper;

    private Docente docente;

    @BeforeEach
    void setUp() {
        docente = TestDataFactory.docente(
                "DOC001", "Carlos", "Méndez",
                Docente.TipoFuncionario.CARRERA, Docente.Rol.DOCENTE, 5, 8.0);
        docente.setPrimerAcceso(1);
        docente = docenteRepository.save(docente);
    }

    @Test
    @DisplayName("Primer acceso detectado en login (primerAcceso=1)")
    void loginDetectaPrimerAcceso() throws Exception {
        mockMvc.perform(post("/api/docentes/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("codigoProfesor", "DOC001", "passwordHash", "1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primerAcceso").value(1));
    }

    @Test
    @DisplayName("Cambio de contraseña actualiza hash y pone primerAcceso=0")
    void cambiarPassword_actualizaYDesactivaPrimerAcceso() throws Exception {
        mockMvc.perform(put("/api/docentes/" + docente.getId() + "/cambiar-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("nuevaPassword", "nuevaPass123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primerAcceso").value(0));

        Docente actualizado = docenteRepository.findById(docente.getId()).orElseThrow();
        assertThat(actualizado.getPasswordHash()).isEqualTo("nuevaPass123");
        assertThat(actualizado.getPrimerAcceso()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cambio de contraseña vacía devuelve 400")
    void cambiarPassword_vacia_devuelve400() throws Exception {
        mockMvc.perform(put("/api/docentes/" + docente.getId() + "/cambiar-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("nuevaPassword", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Tras cambiar contraseña, login con nueva contraseña funciona")
    void loginConNuevaPasswordFunciona() throws Exception {
        // Cambiar primero
        mockMvc.perform(put("/api/docentes/" + docente.getId() + "/cambiar-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("nuevaPassword", "pass2026"))))
                .andExpect(status().isOk());

        // Login con la nueva
        mockMvc.perform(post("/api/docentes/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("codigoProfesor", "DOC001", "passwordHash", "pass2026"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primerAcceso").value(0));
    }
}
