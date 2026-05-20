package gestion_docentes_back.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "guardias")
@Data
public class Guardia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ausencia_id", nullable = false, unique = true)
    private Ausencia ausencia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "docente_guardia_id", nullable = false)
    private Docente docenteGuardia;

    @Enumerated(EnumType.STRING)
    private EstadoGuardia estado;

    private String observaciones;

    @Column(name = "url_material_guardia")
    private String urlMaterialGuardia;

    @PrePersist
    public void prePersist() {
        if (estado == null) estado = EstadoGuardia.PENDIENTE;
    }

    public enum EstadoGuardia {
        PENDIENTE, REALIZADA, INCIDENCIA
    }
}
