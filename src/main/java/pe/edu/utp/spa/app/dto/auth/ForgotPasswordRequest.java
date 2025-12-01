package pe.edu.utp.spa.app.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank(message = "El email es requerido")
        @Email(message = "El email debe ser válido")
        String email
) {}
