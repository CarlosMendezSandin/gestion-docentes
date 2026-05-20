package gestion_docentes_back.service;

import gestion_docentes_back.model.*;
import gestion_docentes_back.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class GuardiaService {

    @Autowired
    private AusenciaRepository ausenciaRepository;

    @Autowired
    private GuardiaRepository guardiaRepository;

    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private DocenteRepository docenteRepository;

    public List<Guardia> listarTodas() {
        return guardiaRepository.findAll();
    }

    public List<Guardia> listarPorFecha(LocalDate fecha) {
        return guardiaRepository.findByFecha(fecha);
    }

    public Ausencia registrarAusencia(Ausencia ausencia) {
        Ausencia guardada = ausenciaRepository.save(ausencia);
        asignarGuardia(guardada);
        return guardada;
    }

    public List<Ausencia> listarAusencias() {
        return ausenciaRepository.findAll();
    }

    public Guardia actualizarEstadoGuardia(Integer guardiaId, Guardia.EstadoGuardia estado, String observaciones) {
        Guardia g = guardiaRepository.findById(guardiaId)
                .orElseThrow(() -> new RuntimeException("Guardia no encontrada"));
        g.setEstado(estado);
        if (observaciones != null) g.setObservaciones(observaciones);
        return guardiaRepository.save(g);
    }

    /**
     * Algoritmo de asignación de guardias:
     *   1. Mismo departamento que el ausente → el que lleve menos guardias
     *   2. Da clase al mismo grupo en ese tramo → el que lleve menos guardias
     *   3. El que lleve menos guardias realizadas en general
     */
    private void asignarGuardia(Ausencia ausencia) {
        Docente ausente = ausencia.getDocente();
        LocalDate fecha = ausencia.getFecha();
        Integer tramo = ausencia.getTramoHorario();
        Horario.DiaSemana diaSemana = localDateToDiaSemana(fecha);

        List<Docente> todosDocentes = docenteRepository.findAll();

        // Candidatos = todos excepto el ausente
        List<Docente> candidatos = todosDocentes.stream()
                .filter(d -> !d.getId().equals(ausente.getId()))
                .toList();

        Docente elegido = null;

        // Regla 1: mismo departamento, menos guardias
        List<Docente> mismoDepartamento = candidatos.stream()
                .filter(d -> d.getDepartamento().equalsIgnoreCase(ausente.getDepartamento()))
                .toList();

        if (!mismoDepartamento.isEmpty()) {
            elegido = menosGuardias(mismoDepartamento);
        }

        // Regla 2: da clase al mismo grupo en ese tramo
        if (elegido == null && diaSemana != null) {
            List<Horario> horariosDelTramo = horarioRepository
                    .findByDiaSemanaAndTramoHorario(diaSemana, tramo);

            // Buscar que grupo da el ausente en ese tramo
            String grupoAusente = horarioRepository.findByDocenteId(ausente.getId()).stream()
                    .filter(h -> h.getDiaSemana() == diaSemana && h.getTramoHorario().equals(tramo))
                    .map(Horario::getGrupoAula)
                    .findFirst()
                    .orElse(null);

            if (grupoAusente != null) {
                String grupoFinal = grupoAusente;
                List<Docente> mismoGrupo = horariosDelTramo.stream()
                        .filter(h -> h.getGrupoAula().equals(grupoFinal))
                        .map(Horario::getDocente)
                        .filter(d -> !d.getId().equals(ausente.getId()))
                        .toList();

                if (!mismoGrupo.isEmpty()) {
                    elegido = menosGuardias(mismoGrupo);
                }
            }
        }

        // Regla 3: el que lleve menos guardias en general
        if (elegido == null) {
            elegido = menosGuardias(candidatos);
        }

        if (elegido != null) {
            Guardia guardia = new Guardia();
            guardia.setAusencia(ausencia);
            guardia.setDocenteGuardia(elegido);
            guardia.setEstado(Guardia.EstadoGuardia.PENDIENTE);
            guardiaRepository.save(guardia);
        }
    }

    private Docente menosGuardias(List<Docente> candidatos) {
        return candidatos.stream()
                .min(Comparator.comparingLong(d ->
                        guardiaRepository.countGuardiasRealizadas(d.getId())))
                .orElse(null);
    }

    private Horario.DiaSemana localDateToDiaSemana(LocalDate fecha) {
        return switch (fecha.getDayOfWeek()) {
            case MONDAY -> Horario.DiaSemana.LUNES;
            case TUESDAY -> Horario.DiaSemana.MARTES;
            case WEDNESDAY -> Horario.DiaSemana.MIERCOLES;
            case THURSDAY -> Horario.DiaSemana.JUEVES;
            case FRIDAY -> Horario.DiaSemana.VIERNES;
            default -> null; // fin de semana no tiene guardias
        };
    }
}
