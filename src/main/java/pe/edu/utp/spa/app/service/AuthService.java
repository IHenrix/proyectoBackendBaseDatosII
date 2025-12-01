package pe.edu.utp.spa.app.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.spa.app.dao.IntentoLoginDao;
import pe.edu.utp.spa.app.dao.ParametroSistemaDao;
import pe.edu.utp.spa.app.dao.PermisoDao;
import pe.edu.utp.spa.app.dao.RolDao;
import pe.edu.utp.spa.app.dao.TokenRecuperacionPasswordDao;
import pe.edu.utp.spa.app.dao.UsuarioDao;
import pe.edu.utp.spa.app.dto.auth.LoginRequest;
import pe.edu.utp.spa.app.dto.auth.LoginResponse;
import pe.edu.utp.spa.app.dto.auth.RefreshTokenRequest;
import pe.edu.utp.spa.app.dto.common.ApiResponse;
import pe.edu.utp.spa.app.exception.BadRequestException;
import pe.edu.utp.spa.app.exception.NotFoundException;
import pe.edu.utp.spa.app.exception.UnauthorizedException;
import pe.edu.utp.spa.app.model.TokenRecuperacionPassword;
import pe.edu.utp.spa.app.model.Usuario;
import pe.edu.utp.spa.app.security.JwtTokenProvider;
import pe.edu.utp.spa.app.util.GeoLocationInfo;
import pe.edu.utp.spa.app.util.UserAgentInfo;
import pe.edu.utp.spa.app.util.UserAgentParser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioDao usuarioDao;
    private final RolDao rolDao;
    private final PermisoDao permisoDao;
    private final IntentoLoginDao intentoLoginDao;
    private final TokenRecuperacionPasswordDao tokenRecuperacionPasswordDao;
    private final ParametroSistemaDao parametroSistemaDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final GeoLocationService geoLocationService;
    private final EmailService emailService;

    public AuthService(
            UsuarioDao usuarioDao,
            RolDao rolDao,
            PermisoDao permisoDao,
            IntentoLoginDao intentoLoginDao,
            TokenRecuperacionPasswordDao tokenRecuperacionPasswordDao,
            ParametroSistemaDao parametroSistemaDao,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            GeoLocationService geoLocationService,
            EmailService emailService
    ) {
        this.usuarioDao = usuarioDao;
        this.rolDao = rolDao;
        this.permisoDao = permisoDao;
        this.intentoLoginDao = intentoLoginDao;
        this.tokenRecuperacionPasswordDao = tokenRecuperacionPasswordDao;
        this.parametroSistemaDao = parametroSistemaDao;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.geoLocationService = geoLocationService;
        this.emailService = emailService;
    }

    @Transactional(noRollbackFor = UnauthorizedException.class)
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        // Extraer información de la solicitud
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        UserAgentInfo userAgentInfo = UserAgentParser.parse(userAgent);
        GeoLocationInfo geoInfo = geoLocationService.getGeoLocation(ipAddress);

        // Obtener configuración de intentos máximos permitidos
        int maxIntentosLogin = parametroSistemaDao.getValorInt("MAX_INTENTOS_LOGIN", 3);

        Usuario usuario = usuarioDao.findByUsername(request.username())
                .orElseThrow(() -> {
                    intentoLoginDao.registrarIntento(
                            null, request.username(), "FAIL", "Usuario no encontrado",
                            ipAddress, userAgent,
                            userAgentInfo.getNavegador(),
                            userAgentInfo.getSistemaOperativo(),
                            userAgentInfo.getDispositivo(),
                            geoInfo.getPais(),
                            geoInfo.getCiudad()
                    );
                    return new UnauthorizedException("Credenciales invalidas");
                });

        // Verificar si la cuenta está bloqueada
        if ("B".equals(usuario.getEstado())) {
            intentoLoginDao.registrarIntento(
                    usuario.getUsuarioId(), usuario.getUsername(), "FAIL", "Cuenta bloqueada",
                    ipAddress, userAgent,
                    userAgentInfo.getNavegador(),
                    userAgentInfo.getSistemaOperativo(),
                    userAgentInfo.getDispositivo(),
                    geoInfo.getPais(),
                    geoInfo.getCiudad()
            );
            throw new UnauthorizedException("Su cuenta ha sido bloqueada por múltiples intentos fallidos. Contacte al administrador para desbloquearla");
        }

        // Verificar si la cuenta está inactiva
        if ("I".equals(usuario.getEstado())) {
            intentoLoginDao.registrarIntento(
                    usuario.getUsuarioId(), usuario.getUsername(), "FAIL", "Cuenta inactiva",
                    ipAddress, userAgent,
                    userAgentInfo.getNavegador(),
                    userAgentInfo.getSistemaOperativo(),
                    userAgentInfo.getDispositivo(),
                    geoInfo.getPais(),
                    geoInfo.getCiudad()
            );
            throw new UnauthorizedException("Usuario inactivo. Contacte al administrador.");
        }

        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            int nuevosIntentos = usuario.getIntentosFallidos() + 1;

            System.out.println("=== DEBUG BLOQUEO ===");
            System.out.println("Usuario: " + usuario.getUsername());
            System.out.println("Intentos previos: " + usuario.getIntentosFallidos());
            System.out.println("Nuevos intentos: " + nuevosIntentos);
            System.out.println("Max intentos permitidos: " + maxIntentosLogin);
            System.out.println("¿Se bloqueará?: " + (nuevosIntentos >= maxIntentosLogin));
            System.out.println("===================");

            usuarioDao.actualizarIntentosFallidos(usuario.getUsuarioId(), nuevosIntentos);

            // Verificar si debe bloquearse la cuenta
            if (nuevosIntentos >= maxIntentosLogin) {
                usuarioDao.bloquearCuenta(usuario.getUsuarioId());
                intentoLoginDao.registrarIntento(
                        usuario.getUsuarioId(), usuario.getUsername(), "FAIL",
                        "Password incorrecto - Cuenta bloqueada por exceder " + maxIntentosLogin + " intentos",
                        ipAddress, userAgent,
                        userAgentInfo.getNavegador(),
                        userAgentInfo.getSistemaOperativo(),
                        userAgentInfo.getDispositivo(),
                        geoInfo.getPais(),
                        geoInfo.getCiudad()
                );
                throw new UnauthorizedException("Su cuenta ha sido bloqueada por múltiples intentos fallidos. Contacte al administrador para desbloquearla");
            }

            intentoLoginDao.registrarIntento(
                    usuario.getUsuarioId(), usuario.getUsername(), "FAIL", "Password incorrecto",
                    ipAddress, userAgent,
                    userAgentInfo.getNavegador(),
                    userAgentInfo.getSistemaOperativo(),
                    userAgentInfo.getDispositivo(),
                    geoInfo.getPais(),
                    geoInfo.getCiudad()
            );
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

        intentoLoginDao.registrarIntento(
                usuario.getUsuarioId(), usuario.getUsername(), "SUCCESS", null,
                ipAddress, userAgent,
                userAgentInfo.getNavegador(),
                userAgentInfo.getSistemaOperativo(),
                userAgentInfo.getDispositivo(),
                geoInfo.getPais(),
                geoInfo.getCiudad()
        );

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

    @Transactional
    public ApiResponse solicitarRecuperacionPassword(String email, HttpServletRequest httpRequest) {
        // Buscar usuario por email en la tabla persona
        Usuario usuario = usuarioDao.findAllActivos().stream()
                .filter(u -> u.getPersona().getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No existe un usuario registrado con ese email"));

        // Invalidar tokens anteriores del usuario
        tokenRecuperacionPasswordDao.invalidarTokensAnteriores(usuario.getUsuarioId());

        // Generar token UUID + JWT (ambos como solicitaste)
        String tokenUUID = UUID.randomUUID().toString();
        String tokenJWT = jwtTokenProvider.generateResetPasswordToken(usuario.getUsuarioId(), email);
        String tokenCombinado = tokenUUID + ":" + tokenJWT;

        // Crear registro de token
        String ipAddress = getClientIp(httpRequest);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = now.plusMinutes(15); // 15 minutos de validez

        TokenRecuperacionPassword token = TokenRecuperacionPassword.builder()
                .usuarioId(usuario.getUsuarioId())
                .token(tokenCombinado)
                .email(email)
                .fechaSolicitud(now)
                .fechaExpiracion(expiration)
                .usado(false)
                .ipSolicitud(ipAddress)
                .build();

        tokenRecuperacionPasswordDao.insert(token);

        // Enviar email
        String nombreCompleto = usuario.getPersona().getNombres() + " " + usuario.getPersona().getApellidoPaterno();
        emailService.enviarEmailRecuperacionPassword(email, nombreCompleto, tokenCombinado);

        return new ApiResponse(true, "Se ha enviado un correo electrónico con instrucciones para recuperar su contraseña");
    }

    @Transactional(readOnly = true)
    public ApiResponse validarTokenRecuperacion(String token) {
        TokenRecuperacionPassword tokenRecuperacion = tokenRecuperacionPasswordDao.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Token inválido o no encontrado"));

        if (tokenRecuperacion.getUsado()) {
            throw new BadRequestException("Este token ya ha sido utilizado");
        }

        if (LocalDateTime.now().isAfter(tokenRecuperacion.getFechaExpiracion())) {
            throw new BadRequestException("Este token ha expirado");
        }

        return new ApiResponse(true, "Token válido");
    }

    @Transactional
    public ApiResponse resetearPassword(String token, String newPassword, HttpServletRequest httpRequest) {
        TokenRecuperacionPassword tokenRecuperacion = tokenRecuperacionPasswordDao.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Token inválido o no encontrado"));

        if (tokenRecuperacion.getUsado()) {
            throw new BadRequestException("Este token ya ha sido utilizado");
        }

        if (LocalDateTime.now().isAfter(tokenRecuperacion.getFechaExpiracion())) {
            throw new BadRequestException("Este token ha expirado");
        }

        // Actualizar contraseña del usuario
        Usuario usuario = usuarioDao.findById(tokenRecuperacion.getUsuarioId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        usuario.setPasswordHash(passwordEncoder.encode(newPassword));
        usuarioDao.update(usuario.getUsuarioId(), usuario, true);

        // Marcar token como usado
        String ipUso = getClientIp(httpRequest);
        tokenRecuperacionPasswordDao.marcarComoUsado(tokenRecuperacion.getTokenId(), ipUso);

        return new ApiResponse(true, "Contraseña actualizada exitosamente");
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
