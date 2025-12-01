package pe.edu.utp.spa.app.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.spa.app.dao.IntentoLoginDao;
import pe.edu.utp.spa.app.dao.PermisoDao;
import pe.edu.utp.spa.app.dao.RolDao;
import pe.edu.utp.spa.app.dao.UsuarioDao;
import pe.edu.utp.spa.app.dto.auth.LoginRequest;
import pe.edu.utp.spa.app.dto.auth.LoginResponse;
import pe.edu.utp.spa.app.dto.auth.RefreshTokenRequest;
import pe.edu.utp.spa.app.exception.UnauthorizedException;
import pe.edu.utp.spa.app.model.Usuario;
import pe.edu.utp.spa.app.security.JwtTokenProvider;

import java.util.List;

@Service
public class AuthService {

    private final UsuarioDao usuarioDao;
    private final RolDao rolDao;
    private final PermisoDao permisoDao;
    private final IntentoLoginDao intentoLoginDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UsuarioDao usuarioDao, RolDao rolDao, PermisoDao permisoDao, IntentoLoginDao intentoLoginDao, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.usuarioDao = usuarioDao;
        this.rolDao = rolDao;
        this.permisoDao = permisoDao;
        this.intentoLoginDao = intentoLoginDao;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioDao.findActivoByUsername(request.username())
                .orElseThrow(() -> {
                    intentoLoginDao.registrarIntento(null, request.username(), "FAIL", "Usuario no encontrado");
                    return new UnauthorizedException("Credenciales invalidas");
                });

        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            int nuevosIntentos = usuario.getIntentosFallidos() + 1;
            usuarioDao.actualizarIntentosFallidos(usuario.getUsuarioId(), nuevosIntentos);
            intentoLoginDao.registrarIntento(usuario.getUsuarioId(), usuario.getUsername(), "FAIL", "Password incorrecto");
            throw new UnauthorizedException("Credenciales invalidas");
        }

        usuarioDao.actualizarIntentosFallidos(usuario.getUsuarioId(), 0);
        usuarioDao.actualizarUltimoAcceso(usuario.getUsuarioId());

        List<String> roles = rolDao.findByUsuario(usuario.getUsuarioId()).stream()
                .map(r -> r.getNombreRol())
                .toList();
        List<String> permisos = permisoDao.findByUsuario(usuario.getUsuarioId()).stream()
                .map(p -> p.getNombrePermiso())
                .toList();

        String accessToken = jwtTokenProvider.generateAccessToken(usuario, roles, permisos);
        String refreshToken = jwtTokenProvider.generateRefreshToken(usuario, roles);

        intentoLoginDao.registrarIntento(usuario.getUsuarioId(), usuario.getUsername(), "SUCCESS", null);

        return new LoginResponse(true, "Login exitoso", accessToken, refreshToken, "Bearer",
                usuario.getUsuarioId(), usuario.getUsername(), usuario.getTipoUsuario(),
                usuario.getPersona().getNombres(),
                usuario.getPersona().getApellidoPaterno(),
                usuario.getPersona().getApellidoMaterno(),
                usuario.getPersona().getEmail(),
                usuario.getPersona().getTelefono(),
                roles, permisos);
    }

    @Transactional(readOnly = true)
    public LoginResponse refresh(RefreshTokenRequest request) {
        String username = jwtTokenProvider.getUsernameFromToken(request.refreshToken());
        if (!jwtTokenProvider.validateRefreshToken(request.refreshToken())) {
            throw new UnauthorizedException("Refresh token invalido");
        }
        Usuario usuario = usuarioDao.findActivoByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado o inactivo"));

        List<String> roles = rolDao.findByUsuario(usuario.getUsuarioId()).stream()
                .map(r -> r.getNombreRol())
                .toList();
        List<String> permisos = permisoDao.findByUsuario(usuario.getUsuarioId()).stream()
                .map(p -> p.getNombrePermiso())
                .toList();

        String newAccessToken = jwtTokenProvider.generateAccessToken(usuario, roles, permisos);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(usuario, roles);

        return new LoginResponse(true, "Token renovado", newAccessToken, newRefreshToken, "Bearer",
                usuario.getUsuarioId(), usuario.getUsername(), usuario.getTipoUsuario(),
                usuario.getPersona().getNombres(),
                usuario.getPersona().getApellidoPaterno(),
                usuario.getPersona().getApellidoMaterno(),
                usuario.getPersona().getEmail(),
                usuario.getPersona().getTelefono(),
                roles, permisos);
    }
}
