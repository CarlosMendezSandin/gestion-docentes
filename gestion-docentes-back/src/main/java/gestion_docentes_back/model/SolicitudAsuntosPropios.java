package gestion_docentes_back.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_asuntos_propios")
@Data
public class SolicitudAsuntosPropios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "docente_id", nullable = false)
    private Docente docente;

    @Column(name = "fecha_solicitada", nullable = false)
    private LocalDate fechaSolicitada;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_dia", nullable = false)
    private TipoDia tipoDia;

    private String justificacion;

    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estado;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
        if (estado == null) estado = EstadoSolicitud.PENDIENTE;
    }

    public enum TipoDia {
        TRIMESTRAL, NO_LECTIVO
    }

    public enum EstadoSolicitud {
        PENDIENTE, APROBADA, DENEGADA
    }
}
