package gestion_docentes_back.repository;

import gestion_docentes_back.model.Ausencia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AusenciaRepository extends JpaRepository<Ausencia, Integer> {

    List<Ausencia> findByFecha(LocalDate fecha);

    List<Ausencia> findByDocenteId(Integer docenteId);

    List<Ausencia> findByFechaAndTramoHorario(LocalDate fecha, Integer tramoHorario);
}
