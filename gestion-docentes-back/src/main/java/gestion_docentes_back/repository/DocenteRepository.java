package gestion_docentes_back.repository;

import gestion_docentes_back.model.Docente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DocenteRepository extends JpaRepository<Docente, Integer> {

    Optional<Docente> findByCodigoProfesorAndPasswordHash(String codigoProfesor, String passwordHash);

    Optional<Docente> findByCodigoProfesor(String codigoProfesor);

    Optional<Docente> findByEmail(String email);
}