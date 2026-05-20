package gestion_docentes_back.repository;

import gestion_docentes_back.model.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HorarioRepository extends JpaRepository<Horario, Integer> {

    List<Horario> findByDocenteId(Integer docenteId);

    List<Horario> findByDiaSemanaAndTramoHorario(
            Horario.DiaSemana diaSemana, Integer tramoHorario);

    List<Horario> findByGrupoAula(String grupoAula);
}
