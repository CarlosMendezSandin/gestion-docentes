package gestion_docentes_back.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void enviarNotificacionAsuntoPropio(String destinatario, String nombreDocente,
                                               String fecha, boolean aprobado) {
        if (mailSender == null || destinatario == null || destinatario.isBlank()) return;
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(destinatario);
            msg.setSubject(aprobado
                    ? "✅ Solicitud de asunto propio APROBADA — " + fecha
                    : "❌ Solicitud de asunto propio DENEGADA — " + fecha);
            msg.setText(aprobado
                    ? "Estimado/a " + nombreDocente + ",\n\n"
                      + "Su solicitud de asunto propio para el día " + fecha + " ha sido APROBADA.\n\n"
                      + "Recuerde acceder a la plataforma para subir el material necesario para cubrir su guardia.\n\n"
                      + "Un saludo,\nCIFP La Laboral"
                    : "Estimado/a " + nombreDocente + ",\n\n"
                      + "Su solicitud de asunto propio para el día " + fecha + " ha sido DENEGADA "
                      + "porque se ha alcanzado el cupo máximo de ausencias permitidas ese día.\n\n"
                      + "Un saludo,\nCIFP La Laboral");
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("[EmailService] No se pudo enviar el correo a " + destinatario + ": " + e.getMessage());
        }
    }
}
