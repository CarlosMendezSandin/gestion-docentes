package gestion_docentes_back.repository;

import gestion_docentes_back.model.SolicitudAsuntosPropios;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface SolicitudAsuntosPropiosRepository extends JpaRepository<SolicitudAsuntosPropios, Integer> {

    List<SolicitudAsuntosPropios> findByDocenteId(Integer docenteId);

    List<SolicitudAsuntosPropios> findByFechaSolicitadaAndEstado(
            LocalDate fechaSolicitada,
            SolicitudAsuntosPropios.EstadoSolicitud estado);

    long countByFechaSolicitadaAndEstado(
            LocalDate fechaSolicitada,
            SolicitudAsuntosPropios.EstadoSolicitud estado);
}
