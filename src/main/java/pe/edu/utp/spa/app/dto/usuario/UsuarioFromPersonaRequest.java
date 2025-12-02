package pe.edu.utp.spa.app.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UsuarioFromPersonaRequest(
        @NotNull Long personaId,
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String tipoUsuario,
        @NotEmpty List<Long> rolesIds
) {}
