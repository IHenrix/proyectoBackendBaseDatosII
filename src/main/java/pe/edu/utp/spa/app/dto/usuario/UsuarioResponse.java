package pe.edu.utp.spa.app.dto.usuario;

import java.util.List;

public record UsuarioResponse(
        Long usuarioId,
        String username,
        String tipoUsuario,
        String estado,
        PersonaDto persona,
        List<String> roles
) {}
