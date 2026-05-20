package gestion_docentes_back.service;

import gestion_docentes_back.model.Docente;
import gestion_docentes_back.model.SolicitudAsuntosPropios;
import gestion_docentes_back.repository.SolicitudAsuntosPropiosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class AsuntosPropiosService {

    @Autowired
    private SolicitudAsuntosPropiosRepository repository;

    @Autowired
    private ConfiguracionService configuracionService;

    @Autowired
    private EmailService emailService;

    public List<SolicitudAsuntosPropios> listarTodas() {
        return repository.findAll();
    }

    public List<SolicitudAsuntosPropios> listarPorDocente(Integer docenteId) {
        return repository.findByDocenteId(docenteId);
    }

    public SolicitudAsuntosPropios crearSolicitud(SolicitudAsuntosPropios solicitud) {
        solicitud.setEstado(SolicitudAsuntosPropios.EstadoSolicitud.PENDIENTE);
        return repository.save(solicitud);
    }

    public boolean puedeAprobarse(SolicitudAsuntosPropios solicitud) {
        int max = configuracionService.getMaxAsuntosPropiosDia();
        long aprobadas = repository.countByFechaSolicitadaAndEstado(
                solicitud.getFechaSolicitada(),
                SolicitudAsuntosPropios.EstadoSolicitud.APROBADA);
        return aprobadas < max;
    }

    public void eliminar(Integer id) {
        repository.deleteById(id);
    }

    public SolicitudAsuntosPropios actualizarEstado(Integer id, SolicitudAsuntosPropios.EstadoSolicitud estado) {
        SolicitudAsuntosPropios s = repository.findById(id).orElseThrow();
        if (estado == SolicitudAsuntosPropios.EstadoSolicitud.APROBADA && !puedeAprobarse(s)) {
            throw new RuntimeException("Cupo máximo de " + configuracionService.getMaxAsuntosPropiosDia()
                    + " ausencias alcanzado para este día.");
        }
        s.setEstado(estado);
        repository.save(s);

        // Notificación por email
        boolean aprobado = estado == SolicitudAsuntosPropios.EstadoSolicitud.APROBADA;
        String email = s.getDocente().getEmail();
        String nombre = s.getDocente().getNombre() + " " + s.getDocente().getApellidos();
        emailService.enviarNotificacionAsuntoPropio(email, nombre,
                s.getFechaSolicitada().toString(), aprobado);

        return s;
    }

    public SolicitudAsuntosPropios procesarSolicitud(SolicitudAsuntosPropios solicitud) {
        if (!puedeAprobarse(solicitud)) {
            solicitud.setEstado(SolicitudAsuntosPropios.EstadoSolicitud.DENEGADA);
            repository.save(solicitud);
            emailService.enviarNotificacionAsuntoPropio(
                    solicitud.getDocente().getEmail(),
                    solicitud.getDocente().getNombre() + " " + solicitud.getDocente().getApellidos(),
                    solicitud.getFechaSolicitada().toString(), false);
            return solicitud;
        }

        int max = configuracionService.getMaxAsuntosPropiosDia();
        List<SolicitudAsuntosPropios> pendientesDelDia = repository
                .findByFechaSolicitadaAndEstado(
                        solicitud.getFechaSolicitada(),
                        SolicitudAsuntosPropios.EstadoSolicitud.PENDIENTE);

        pendientesDelDia.sort(Comparator
                .comparingInt((SolicitudAsuntosPropios s) ->
                        tipoFuncionarioPrioridad(s.getDocente().getTipoFuncionario()))
                .thenComparingInt(s -> -(s.getDocente().getAntiguedadCentro() != null
                        ? s.getDocente().getAntiguedadCentro() : 0))
                .thenComparingDouble(s -> -(s.getDocente().getNotaOposicion() != null
                        ? s.getDocente().getNotaOposicion() : 0.0)));

        long yaAprobadas = repository.countByFechaSolicitadaAndEstado(
                solicitud.getFechaSolicitada(),
                SolicitudAsuntosPropios.EstadoSolicitud.APROBADA);
        long cuposDisponibles = max - yaAprobadas;

        for (int i = 0; i < pendientesDelDia.size(); i++) {
            SolicitudAsuntosPropios s = pendientesDelDia.get(i);
            boolean aprobado = i < cuposDisponibles;
            s.setEstado(aprobado
                    ? SolicitudAsuntosPropios.EstadoSolicitud.APROBADA
                    : SolicitudAsuntosPropios.EstadoSolicitud.DENEGADA);
            repository.save(s);
            emailService.enviarNotificacionAsuntoPropio(
                    s.getDocente().getEmail(),
                    s.getDocente().getNombre() + " " + s.getDocente().getApellidos(),
                    s.getFechaSolicitada().toString(), aprobado);
        }

        return repository.findById(solicitud.getId()).orElse(solicitud);
    }

    private int tipoFuncionarioPrioridad(Docente.TipoFuncionario tipo) {
        if (tipo == null) return 99;
        return switch (tipo) {
            case CARRERA -> 1;
            case PRACTICAS -> 2;
            case INTERINO -> 3;
        };
    }
}
