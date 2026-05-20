package gestion_docentes_back.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "horarios")
@Data
public class Horario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "docente_id", nullable = false)
    private Docente docente;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false)
    private DiaSemana diaSemana;

    @Column(name = "tramo_horario", nullable = false)
    private Integer tramoHorario;

    @Column(name = "grupo_aula", nullable = false)
    private String grupoAula;

    public enum DiaSemana {
        LUNES, MARTES, MIERCOLES, JUEVES, VIERNES
    }
}
