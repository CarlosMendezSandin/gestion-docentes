package gestion_docentes_back;

import com.fasterxml.jackson.databind.ObjectMapper;
import gestion_docentes_back.model.Docente;
import gestion_docentes_back.model.SolicitudAsuntosPropios;
import gestion_docentes_back.repository.DocenteRepository;
import gestion_docentes_back.repository.SolicitudAsuntosPropiosRepository;
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

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("UC2 - Solicitar Día Propio")
class UC2_SolicitarDiaPropioTest {

    @Autowired MockMvc mockMvc;
    @Autowired DocenteRepository docenteRepository;
    @Autowired SolicitudAsuntosPropiosRepository solicitudRepository;
    @Autowired ObjectMapper objectMapper;

    private Docente docente;

    @BeforeEach
    void setUp() {
        docente = docenteRepository.save(TestDataFactory.docente(
                "DOC001", "Ana", "García",
                Docente.TipoFuncionario.CARRERA, Docente.Rol.DOCENTE, 10, 7.5));
    }

    @Test
    @DisplayName("Docente crea solicitud → 201 con estado PENDIENTE")
    void crearSolicitud_estadoPendiente() throws Exception {
        var body = Map.of(
                "docente", Map.of("id", docente.getId()),
                "fechaSolicitada", LocalDate.now().plusDays(5).toString(),
                "tipoDia", "TRIMESTRAL",
                "justificacion", "Gestión personal"
        );

        mockMvc.perform(post("/api/asuntos-propios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.tipoDia").value("TRIMESTRAL"));
    }

    @Test
    @DisplayName("Solicitud tipo NO_LECTIVO se crea correctamente")
    void crearSolicitudNoLectivo() throws Exception {
        var body = Map.of(
                "docente", Map.of("id", docente.getId()),
                "fechaSolicitada", LocalDate.now().plusDays(10).toString(),
                "tipoDia", "NO_LECTIVO",
                "justificacion", "Día personal"
        );

        mockMvc.perform(post("/api/asuntos-propios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoDia").value("NO_LECTIVO"));
    }

    @Test
    @DisplayName("UC6 - Docente consulta sus propias solicitudes")
    void consultarSolicitudesPorDocente() throws Exception {
        // Crear una solicitud previa
        SolicitudAsuntosPropios s = new SolicitudAsuntosPropios();
        s.setDocente(docente);
        s.setFechaSolicitada(LocalDate.now().plusDays(3));
        s.setTipoDia(SolicitudAsuntosPropios.TipoDia.TRIMESTRAL);
        s.setEstado(SolicitudAsuntosPropios.EstadoSolicitud.PENDIENTE);
        solicitudRepository.save(s);

        mockMvc.perform(get("/api/asuntos-propios/docente/" + docente.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].docente.id").value(docente.getId()));
    }
}
