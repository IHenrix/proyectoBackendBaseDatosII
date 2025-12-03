package pe.edu.utp.spa.app.dto.auth;

import java.util.List;

public record LoginResponse(
        boolean success,
        String message,
        String accessToken,
        String refreshToken,
        String tokenType,
        Long usuarioId,
        String username,
        String tipoUsuario,
        String nombres,
        String apellidoPaterno,
        String apellidoMaterno,
        String email,
        String telefono,
        Integer empleadoId,
        List<String> roles,
        List<String> permisos
) {}
