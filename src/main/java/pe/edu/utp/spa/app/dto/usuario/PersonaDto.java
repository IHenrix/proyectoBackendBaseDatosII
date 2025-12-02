package pe.edu.utp.spa.app.dto.usuario;

import java.time.LocalDate;

public record PersonaDto(
        Long personaId,
        String nombres,
        String apellidoPaterno,
        String apellidoMaterno,
        Long tipoDocumentoId,
        String tipoDocumentoNombre,
        String numeroDocumento,
        String email,
        String telefono,
        LocalDate fechaNacimiento,
        String genero,
        String direccion,
        String estado
) {}
