package gestion_docentes_back.repository;

import gestion_docentes_back.model.Guardia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GuardiaRepository extends JpaRepository<Guardia, Integer> {

    Optional<Guardia> findByAusenciaId(Integer ausenciaId);

    List<Guardia> findByDocenteGuardiaId(Integer docenteId);

    @Query("SELECT g FROM Guardia g WHERE g.ausencia.fecha = :fecha")
    List<Guardia> findByFecha(@Param("fecha") LocalDate fecha);

    @Query("SELECT COUNT(g) FROM Guardia g WHERE g.docenteGuardia.id = :docenteId AND g.estado = 'REALIZADA'")
    long countGuardiasRealizadas(@Param("docenteId") Integer docenteId);
}
