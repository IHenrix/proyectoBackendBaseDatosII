package pe.edu.utp.spa.app.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UsuarioAccesoUpdateRequest(
        @NotBlank String username,
        String password,
        @NotBlank String tipoUsuario,
        @NotEmpty List<Long> rolesIds,
        String estado
) {}
