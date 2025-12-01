package pe.edu.utp.spa.app.dto.rol;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RolCreateRequest(
        @NotBlank String nombreRol,
        String descripcion,
        @NotBlank String tipoRol,
        @NotEmpty List<@NotNull Long> permisosIds
) {
}
