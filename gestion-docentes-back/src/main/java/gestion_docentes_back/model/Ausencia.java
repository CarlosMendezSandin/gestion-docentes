package gestion_docentes_back.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "ausencias")
@Data
public class Ausencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "docente_id", nullable = false)
    private Docente docente;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "tramo_horario", nullable = false)
    private Integer tramoHorario;

    private String motivo;
}
