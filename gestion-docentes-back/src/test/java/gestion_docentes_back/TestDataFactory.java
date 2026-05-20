package gestion_docentes_back;

import gestion_docentes_back.model.Docente;
import gestion_docentes_back.model.Horario;

/** Fábrica de objetos de prueba reutilizables en todos los tests. */
public class TestDataFactory {

    public static Docente docente(String codigo, String nombre, String apellidos,
                                  Docente.TipoFuncionario tipo, Docente.Rol rol,
                                  int antiguedad, double nota) {
        Docente d = new Docente();
        d.setCodigoProfesor(codigo);
        d.setNombre(nombre);
        d.setApellidos(apellidos);
        d.setEmail(codigo.toLowerCase() + "@laboral.com");
        d.setDepartamento("Informatica");
        d.setTipoFuncionario(tipo);
        d.setRol(rol);
        d.setAntiguedadCentro(antiguedad);
        d.setNotaOposicion(nota);
        d.setPasswordHash("1234");
        d.setPrimerAcceso(0);
        return d;
    }

    public static Horario horario(Docente docente, Horario.DiaSemana dia,
                                   int tramo, String grupo) {
        Horario h = new Horario();
        h.setDocente(docente);
        h.setDiaSemana(dia);
        h.setTramoHorario(tramo);
        h.setGrupoAula(grupo);
        return h;
    }
}
