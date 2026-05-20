package gestion_docentes_back.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "docentes")
@Data
public class Docente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nombre;
    private String apellidos;
    private String email;
    private String codigoProfesor;
    private String departamento;

    @Enumerated(EnumType.STRING)
    private TipoFuncionario tipoFuncionario;

    private Integer antiguedadCentro;
    private Double notaOposicion;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    private String passwordHash;

    /* Primer acceso cambio de contraseña */
    @Column(name = "primer_acceso")
    private Integer primerAcceso;

    public enum TipoFuncionario {
        CARRERA, PRACTICAS, INTERINO
    }

    public enum Rol {
        ADMIN, DOCENTE
    }

}