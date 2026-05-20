package gestion_docentes_back;

import com.fasterxml.jackson.databind.ObjectMapper;
import gestion_docentes_back.model.Ausencia;
import gestion_docentes_back.model.Docente;
import gestion_docentes_back.model.Horario;
import gestion_docentes_back.repository.AusenciaRepository;
import gestion_docentes_back.repository.DocenteRepository;
import gestion_docentes_back.repository.GuardiaRepository;
import gestion_docentes_back.repository.HorarioRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("UC4 - Introducir Faltas y asignación de guardias")
class UC4_IntroducirFaltasTest {

    @Autowired MockMvc mockMvc;
    @Autowired DocenteRepository docenteRepository;
    @Autowired HorarioRepository horarioRepository;
    @Autowired AusenciaRepository ausenciaRepository;
    @Autowired GuardiaRepository guardiaRepository;
    @Autowired ObjectMapper objectMapper;

    private Docente ausente;
    private Docente sustituto;

    @BeforeEach
    void setUp() {
        ausente = docenteRepository.save(TestDataFactory.docente(
                "DOC001", "Carlos", "Méndez",
                Docente.TipoFuncionario.CARRERA, Docente.Rol.DOCENTE, 5, 8.0));
        sustituto = docenteRepository.save(TestDataFactory.docente(
                "DOC002", "Ana", "García",
                Docente.TipoFuncionario.CARRERA, Docente.Rol.DOCENTE, 3, 7.0));

        // Horario del sustituto en el mismo tramo
        horarioRepository.save(TestDataFactory.horario(
                sustituto, Horario.DiaSemana.LUNES, 1, "1DAW-A"));
    }

    @Test
    @DisplayName("Registrar ausencia crea la guardia automáticamente")
    void registrarAusencia_creaGuardia() throws Exception {
        // El lunes más próximo
        LocalDate lunes = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        if (lunes.isBefore(LocalDate.now())) lunes = lunes.plusWeeks(1);

        var body = Map.of(
                "docente", Map.of("id", ausente.getId()),
                "fecha", lunes.toString(),
                "tramoHorario", 1,
                "motivo", "Enfermedad"
        );

        mockMvc.perform(post("/api/ausencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        // La guardia debe haberse creado
        assertThat(guardiaRepository.findAll()).isNotEmpty();
    }

    @Test
    @DisplayName("La guardia asignada tiene estado PENDIENTE por defecto")
    void guardiaCreada_estadoPendiente() throws Exception {
        LocalDate lunes = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        if (lunes.isBefore(LocalDate.now())) lunes = lunes.plusWeeks(1);

        var body = Map.of(
                "docente", Map.of("id", ausente.getId()),
                "fecha", lunes.toString(),
                "tramoHorario", 1,
                "motivo", "Baja médica"
        );

        mockMvc.perform(post("/api/ausencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        var guardias = guardiaRepository.findAll();
        assertThat(guardias).isNotEmpty();
        assertThat(guardias.get(0).getEstado())
                .isEqualTo(gestion_docentes_back.model.Guardia.EstadoGuardia.PENDIENTE);
    }

    @Test
    @DisplayName("Listar guardias por fecha devuelve las del día correcto")
    void listarGuardiasPorFecha() throws Exception {
        LocalDate hoy = LocalDate.now();

        mockMvc.perform(get("/api/guardias/fecha/" + hoy))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
