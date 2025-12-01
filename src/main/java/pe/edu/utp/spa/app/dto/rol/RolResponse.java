package pe.edu.utp.spa.app.dto.rol;

import java.util.List;

public record RolResponse(
        Long rolId,
        String nombreRol,
        String descripcion,
        String tipoRol,
        String estado,
        List<PermisoDto> permisos
) {
}
