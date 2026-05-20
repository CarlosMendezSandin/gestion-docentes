package gestion_docentes_back;

import gestion_docentes_back.repository.DocenteRepository;
import gestion_docentes_back.repository.HorarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("UC3 - Carga Datos CSV")
class UC3_CargaDatosTest {

    @Autowired MockMvc mockMvc;
    @Autowired DocenteRepository docenteRepository;
    @Autowired HorarioRepository horarioRepository;

    private static final String CSV_DOCENTES =
            "codigoProfesor,nombre,apellidos,email,departamento,tipoFuncionario,antiguedadCentro,notaOposicion,rol,password\n" +
            "DOC010,Marta,López,marta@laboral.com,Matematicas,CARRERA,5,8.0,DOCENTE,1234\n" +
            "DOC011,Pedro,Ruiz,pedro@laboral.com,FOL,INTERINO,1,6.5,DOCENTE,1234\n";

    private static final String CSV_DOCENTES_CON_ERROR =
            "codigoProfesor,nombre,apellidos,email,departamento,tipoFuncionario\n" +
            "DOC012,Luis,García,luis@laboral.com,FP,CARRERA\n" +
            "MAL\n";

    @Test
    @DisplayName("UC9/UC10 - Importar CSV de docentes importa correctamente")
    void importarDocentes_correctamente() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "docentes.csv", "text/csv", CSV_DOCENTES.getBytes());

        mockMvc.perform(multipart("/api/importar/docentes").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importados").value(2))
                .andExpect(jsonPath("$.errores.length()").value(0));

        assertThat(docenteRepository.findByCodigoProfesor("DOC010")).isPresent();
        assertThat(docenteRepository.findByCodigoProfesor("DOC011")).isPresent();
    }

    @Test
    @DisplayName("Importar docente ya existente lo actualiza (no duplica)")
    void importarDocente_existente_actualiza() throws Exception {
        String csvUpdate =
                "codigoProfesor,nombre,apellidos,email,departamento,tipoFuncionario,antiguedadCentro,notaOposicion,rol,password\n" +
                "DOC010,Marta,López Nuevo,marta@laboral.com,Matematicas,CARRERA,6,8.5,DOCENTE,1234\n";

        MockMultipartFile primera = new MockMultipartFile("file", "d.csv", "text/csv", CSV_DOCENTES.getBytes());
        mockMvc.perform(multipart("/api/importar/docentes").file(primera)).andExpect(status().isOk());

        MockMultipartFile segunda = new MockMultipartFile("file", "d2.csv", "text/csv", csvUpdate.getBytes());
        mockMvc.perform(multipart("/api/importar/docentes").file(segunda)).andExpect(status().isOk());

        assertThat(docenteRepository.findAll().stream()
                .filter(d -> "DOC010".equals(d.getCodigoProfesor()))
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("CSV con líneas incompletas reporta errores sin detener importación")
    void importarDocentes_conErrores_reportaYContinua() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "docentes_mal.csv", "text/csv", CSV_DOCENTES_CON_ERROR.getBytes());

        mockMvc.perform(multipart("/api/importar/docentes").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importados").value(1))
                .andExpect(jsonPath("$.errores.length()").value(1));
    }

    @Test
    @DisplayName("UC9 - Importar CSV de horarios crea registros correctamente")
    void importarHorarios_correctamente() throws Exception {
        // Primero crear el docente referenciado
        String csvDocente =
                "codigoProfesor,nombre,apellidos,email,departamento,tipoFuncionario,antiguedadCentro,notaOposicion,rol,password\n" +
                "DOC010,Marta,López,marta@laboral.com,Matematicas,CARRERA,5,8.0,DOCENTE,1234\n";
        mockMvc.perform(multipart("/api/importar/docentes")
                .file(new MockMultipartFile("file", "d.csv", "text/csv", csvDocente.getBytes())))
                .andExpect(status().isOk());

        String csvHorarios =
                "codigoProfesor,diaSemana,tramoHorario,grupoAula\n" +
                "DOC010,LUNES,1,1DAW-A\n" +
                "DOC010,MARTES,2,1DAW-A\n";

        mockMvc.perform(multipart("/api/importar/horarios")
                .file(new MockMultipartFile("file", "horarios.csv", "text/csv", csvHorarios.getBytes())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importados").value(2))
                .andExpect(jsonPath("$.errores.length()").value(0));

        assertThat(horarioRepository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("Importar horario con docente inexistente reporta error")
    void importarHorario_docenteNoExiste_reportaError() throws Exception {
        String csv =
                "codigoProfesor,diaSemana,tramoHorario,grupoAula\n" +
                "NOEXISTE,LUNES,1,1DAW-A\n";

        mockMvc.perform(multipart("/api/importar/horarios")
                .file(new MockMultipartFile("file", "h.csv", "text/csv", csv.getBytes())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importados").value(0))
                .andExpect(jsonPath("$.errores.length()").value(1));
    }
}
