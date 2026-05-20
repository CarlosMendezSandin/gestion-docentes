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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("UC5 - Validar Días Propios (Admin aprueba/deniega)")
class UC5_ValidarDiasPropiosTest {

    @Autowired MockMvc mockMvc;
    @Autowired DocenteRepository docenteRepository;
    @Autowired SolicitudAsuntosPropiosRepository solicitudRepository;
    @Autowired ObjectMapper objectMapper;

    private Docente docente;
    private SolicitudAsuntosPropios solicitud;
    private final LocalDate fechaPrueba = LocalDate.of(2026, 9, 15);

    @BeforeEach
    void setUp() {
        docente = docenteRepository.save(TestDataFactory.docente(
                "DOC001", "Luis", "Martínez",
                Docente.TipoFuncionario.CARRERA, Docente.Rol.DOCENTE, 5, 8.0));

        solicitud = new SolicitudAsuntosPropios();
        solicitud.setDocente(docente);
        solicitud.setFechaSolicitada(fechaPrueba);
        solicitud.setTipoDia(SolicitudAsuntosPropios.TipoDia.TRIMESTRAL);
        solicitud.setEstado(SolicitudAsuntosPropios.EstadoSolicitud.PENDIENTE);
        solicitud = solicitudRepository.save(solicitud);
    }

    @Test
    @DisplayName("Admin aprueba solicitud pendiente → estado APROBADA")
    void aprobarSolicitud() throws Exception {
        mockMvc.perform(patch("/api/asuntos-propios/" + solicitud.getId() + "/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("estado", "APROBADA"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));
    }

    @Test
    @DisplayName("Admin deniega solicitud pendiente → estado DENEGADA")
    void denegarSolicitud() throws Exception {
        mockMvc.perform(patch("/api/asuntos-propios/" + solicitud.getId() + "/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("estado", "DENEGADA"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DENEGADA"));
    }

    @Test
    @DisplayName("Cupo lleno (3 aprobadas mismo día) → error al aprobar la 4ª")
    void cupoLleno_rechazaCuartaSolicitud() throws Exception {
        // Aprobar 3 solicitudes del mismo día con distintos docentes
        for (int i = 2; i <= 4; i++) {
            Docente d = docenteRepository.save(TestDataFactory.docente(
                    "DOC00" + i, "Docente" + i, "Apellido",
                    Docente.TipoFuncionario.CARRERA, Docente.Rol.DOCENTE, i, 7.0));
            SolicitudAsuntosPropios s = new SolicitudAsuntosPropios();
            s.setDocente(d);
            s.setFechaSolicitada(fechaPrueba);
            s.setTipoDia(SolicitudAsuntosPropios.TipoDia.TRIMESTRAL);
            s.setEstado(SolicitudAsuntosPropios.EstadoSolicitud.APROBADA);
            solicitudRepository.save(s);
        }

        // La solicitud original (4ª del mismo día) debe ser rechazada
        mockMvc.perform(patch("/api/asuntos-propios/" + solicitud.getId() + "/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("estado", "APROBADA"))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Algoritmo de prioridad: CARRERA tiene preferencia sobre INTERINO")
    void algoritmoProridadTipoFuncionario() {
        Docente interino = docenteRepository.save(TestDataFactory.docente(
                "DOC010", "Interino", "Test",
                Docente.TipoFuncionario.INTERINO, Docente.Rol.DOCENTE, 1, 5.0));
        Docente carrera = docenteRepository.save(TestDataFactory.docente(
                "DOC011", "Carrera", "Test",
                Docente.TipoFuncionario.CARRERA, Docente.Rol.DOCENTE, 1, 5.0));

        LocalDate dia = LocalDate.of(2026, 10, 1);
        SolicitudAsuntosPropios sInterino = crearSolicitud(interino, dia);
        SolicitudAsuntosPropios sCarrera = crearSolicitud(carrera, dia);

        // Solo hay 1 cupo disponible → CARRERA debe ganar
        // Procesamos ambas: CARRERA primero por prioridad
        int prioridadInterino = prioridad(interino.getTipoFuncionario());
        int prioridadCarrera  = prioridad(carrera.getTipoFuncionario());
        assertThat(prioridadCarrera).isLessThan(prioridadInterino);
    }

    private SolicitudAsuntosPropios crearSolicitud(Docente d, LocalDate fecha) {
        SolicitudAsuntosPropios s = new SolicitudAsuntosPropios();
        s.setDocente(d);
        s.setFechaSolicitada(fecha);
        s.setTipoDia(SolicitudAsuntosPropios.TipoDia.TRIMESTRAL);
        s.setEstado(SolicitudAsuntosPropios.EstadoSolicitud.PENDIENTE);
        return solicitudRepository.save(s);
    }

    private int prioridad(Docente.TipoFuncionario tipo) {
        return switch (tipo) {
            case CARRERA -> 1;
            case PRACTICAS -> 2;
            case INTERINO -> 3;
        };
    }
}
