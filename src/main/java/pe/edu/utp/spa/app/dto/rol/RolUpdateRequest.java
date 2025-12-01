package pe.edu.utp.spa.app.dto.rol;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RolUpdateRequest(
        @NotBlank String nombreRol,
        String descripcion,
        @NotBlank String tipoRol,
        @NotBlank String estado,
        @NotEmpty List<@NotNull Long> permisosIds
) {
}
