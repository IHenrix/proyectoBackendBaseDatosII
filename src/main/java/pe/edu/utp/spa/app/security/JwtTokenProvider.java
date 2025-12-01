package pe.edu.utp.spa.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.edu.utp.spa.app.model.Usuario;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private final javax.crypto.SecretKey signingKey;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long accessTokenValidityMs,
            @Value("${jwt.refresh.expiration}") long refreshTokenValidityMs
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityMs = accessTokenValidityMs;
        this.refreshTokenValidityMs = refreshTokenValidityMs;
    }

    public String generateAccessToken(Usuario usuario, List<String> roles, List<String> permisos) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("uid", usuario.getUsuarioId())
                .claim("tipoUsuario", usuario.getTipoUsuario())
                .claim("roles", roles)
                .claim("permisos", permisos)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessTokenValidityMs)))
                .signWith(signingKey)
                .compact();
    }

    public String generateRefreshToken(Usuario usuario, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("uid", usuario.getUsuarioId())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(refreshTokenValidityMs)))
                .signWith(signingKey)
                .compact();
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token);
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token);
    }

    private boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(signingKey).build().parse(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
        return claims.getSubject();
    }
}
