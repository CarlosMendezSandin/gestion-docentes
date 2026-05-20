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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("UC1 - Login")
class UC1_LoginTest {

    @Autowired MockMvc mockMvc;
    @Autowired DocenteRepository docenteRepository;
    @Autowired ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        docenteRepository.save(TestDataFactory.docente(
                "DOC001", "Carlos", "Méndez",
                Docente.TipoFuncionario.CARRERA, Docente.Rol.ADMIN, 24, 8.5));
    }

    @Test
    @DisplayName("Login correcto devuelve 200 con datos del docente")
    void loginCorrecto() throws Exception {
        mockMvc.perform(post("/api/docentes/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("codigoProfesor", "DOC001", "passwordHash", "1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoProfesor").value("DOC001"))
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    @Test
    @DisplayName("Contraseña incorrecta devuelve 401")
    void loginContrasenaIncorrecta() throws Exception {
        mockMvc.perform(post("/api/docentes/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("codigoProfesor", "DOC001", "passwordHash", "wrongpass"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Usuario inexistente devuelve 404")
    void loginUsuarioNoExiste() throws Exception {
        mockMvc.perform(post("/api/docentes/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("codigoProfesor", "NOEXISTE", "passwordHash", "1234"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Campos vacíos devuelven 400")
    void loginCamposVacios() throws Exception {
        mockMvc.perform(post("/api/docentes/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of())))
                .andExpect(status().isBadRequest());
    }
}
