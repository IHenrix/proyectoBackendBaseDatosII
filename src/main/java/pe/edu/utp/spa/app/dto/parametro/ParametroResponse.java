package pe.edu.utp.spa.app.dto.parametro;

public record ParametroResponse(
        Long parametroId,
        String codigo,
        String descripcion,
        String valor,
        String tipoDato,
        String estado
) {
}
