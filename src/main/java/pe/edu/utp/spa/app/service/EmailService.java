package pe.edu.utp.spa.app.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String fromEmail;
    private final String fromName;
    private final String frontendUrl;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String fromEmail,
            @Value("${app.mail.from-name:Sistema Spa Natural Beauty}") String fromName,
            @Value("${app.frontend.url:http://localhost:4200}") String frontendUrl
    ) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        this.frontendUrl = frontendUrl;
    }

    public void enviarEmailRecuperacionPassword(String destinatario, String nombreUsuario, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(destinatario);
            helper.setSubject("Recuperación de Contraseña - Sistema Spa Natural Beauty");

            String resetLink = frontendUrl + "/reset-password?token=" + token;
            String htmlContent = buildRecuperacionPasswordHtml(nombreUsuario, resetLink);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Email de recuperación enviado exitosamente a: {}", destinatario);

        } catch (Exception e) {
            logger.error("Error al enviar email de recuperación a {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("Error al enviar email de recuperación", e);
        }
    }

    private String buildRecuperacionPasswordHtml(String nombreUsuario, String resetLink) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Recuperación de Contraseña</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4;">
                    <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #f4f4f4; padding: 20px;">
                        <tr>
                            <td align="center">
                                <table border="0" cellpadding="0" cellspacing="0" width="600" style="background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                                    <!-- Header con color del SPA -->
                                    <tr>
                                        <td bgcolor="#6B8E23" style="padding: 40px 30px; text-align: center;">
                                            <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: 300; letter-spacing: 2px;">
                                                SPA NATURAL BEAUTY
                                            </h1>
                                        </td>
                                    </tr>

                                    <!-- Contenido principal -->
                                    <tr>
                                        <td style="padding: 40px 30px;">
                                            <h2 style="color: #333333; margin: 0 0 20px 0; font-size: 24px; font-weight: 400;">
                                                Recuperación de Contraseña
                                            </h2>

                                            <p style="color: #666666; font-size: 16px; line-height: 1.6; margin: 0 0 15px 0;">
                                                Hola <strong style="color: #6B8E23;">%s</strong>,
                                            </p>

                                            <p style="color: #666666; font-size: 16px; line-height: 1.6; margin: 0 0 15px 0;">
                                                Hemos recibido una solicitud para restablecer la contraseña de su cuenta en nuestro sistema.
                                            </p>

                                            <p style="color: #666666; font-size: 16px; line-height: 1.6; margin: 0 0 25px 0;">
                                                Para crear una nueva contraseña, haga clic en el siguiente botón:
                                            </p>

                                            <!-- Botón de acción -->
                                            <table border="0" cellpadding="0" cellspacing="0" width="100%%">
                                                <tr>
                                                    <td align="center" style="padding: 10px 0;">
                                                        <a href="%s" style="display: inline-block; padding: 15px 40px; background-color: #6B8E23; color: #ffffff; text-decoration: none; border-radius: 4px; font-size: 16px; font-weight: 500; transition: background-color 0.3s;">
                                                            Restablecer Contraseña
                                                        </a>
                                                    </td>
                                                </tr>
                                            </table>

                                            <p style="color: #999999; font-size: 14px; line-height: 1.6; margin: 25px 0 15px 0;">
                                                Este enlace expirará en <strong>15 minutos</strong> por motivos de seguridad.
                                            </p>

                                            <p style="color: #999999; font-size: 14px; line-height: 1.6; margin: 0 0 15px 0;">
                                                Si no solicitó restablecer su contraseña, puede ignorar este mensaje de forma segura. Su contraseña actual permanecerá sin cambios.
                                            </p>

                                            <!-- Enlace alternativo -->
                                            <p style="color: #999999; font-size: 12px; line-height: 1.6; margin: 25px 0 0 0; padding-top: 20px; border-top: 1px solid #eeeeee;">
                                                Si el botón no funciona, copie y pegue este enlace en su navegador:
                                            </p>
                                            <p style="color: #6B8E23; font-size: 12px; line-height: 1.6; margin: 5px 0 0 0; word-break: break-all;">
                                                %s
                                            </p>
                                        </td>
                                    </tr>

                                    <!-- Footer -->
                                    <tr>
                                        <td bgcolor="#f8f8f8" style="padding: 30px; text-align: center; border-top: 1px solid #eeeeee;">
                                            <p style="color: #999999; font-size: 14px; line-height: 1.6; margin: 0 0 10px 0;">
                                                Atentamente,<br>
                                                <strong style="color: #6B8E23;">Sistema Spa Natural Beauty</strong>
                                            </p>
                                            <p style="color: #cccccc; font-size: 12px; margin: 10px 0 0 0;">
                                                Este es un correo automático, por favor no responda a este mensaje.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(nombreUsuario, resetLink, resetLink);
    }
}
