package pe.edu.utp.spa.app.dto.parametro;

import jakarta.validation.constraints.NotBlank;

public record ParametroRequest(
        @NotBlank String codigo,
        String descripcion,
        @NotBlank String valor,
        String tipoDato,
        String estado
) {
}
