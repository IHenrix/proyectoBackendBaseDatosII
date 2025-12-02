package pe.edu.utp.spa.app.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PersonaCreateRequest(
        @NotBlank String nombres,
        @NotBlank String apellidoPaterno,
        String apellidoMaterno,
        @NotNull Long tipoDocumentoId,
        @NotBlank String numeroDocumento,
        @Email @NotBlank String email,
        String telefono,
        LocalDate fechaNacimiento,
        String genero,
        String direccion
) {}
