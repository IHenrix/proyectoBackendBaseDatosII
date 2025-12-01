package pe.edu.utp.spa.app.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import pe.edu.utp.spa.app.model.TokenRecuperacionPassword;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;

@Repository
public class TokenRecuperacionPasswordDao {

    private final JdbcTemplate jdbcTemplate;

    public TokenRecuperacionPasswordDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long insert(TokenRecuperacionPassword token) {
        String sql = """
                INSERT INTO token_recuperacion_password
                (usuario_id, token, email, fecha_solicitud, fecha_expiracion, usado, ip_solicitud)
                VALUES (?,?,?,?,?,?,?)
                RETURNING token_id
                """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, token.getUsuarioId());
            ps.setString(2, token.getToken());
            ps.setString(3, token.getEmail());
            ps.setTimestamp(4, Timestamp.valueOf(token.getFechaSolicitud()));
            ps.setTimestamp(5, Timestamp.valueOf(token.getFechaExpiracion()));
            ps.setBoolean(6, false);
            ps.setString(7, token.getIpSolicitud());
            return ps;
        }, kh);
        Number key = kh.getKeys() != null ? (Number) kh.getKeys().get("token_id") : kh.getKey();
        return key.longValue();
    }

    public Optional<TokenRecuperacionPassword> findByToken(String token) {
        String sql = """
                SELECT token_id, usuario_id, token, email, fecha_solicitud, fecha_expiracion,
                       usado, fecha_uso, ip_solicitud, ip_uso
                FROM token_recuperacion_password
                WHERE token=?
                """;
        return jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) return Optional.empty();
            return Optional.of(TokenRecuperacionPassword.builder()
                    .tokenId(rs.getLong("token_id"))
                    .usuarioId(rs.getLong("usuario_id"))
                    .token(rs.getString("token"))
                    .email(rs.getString("email"))
                    .fechaSolicitud(rs.getTimestamp("fecha_solicitud") != null ? rs.getTimestamp("fecha_solicitud").toLocalDateTime() : null)
                    .fechaExpiracion(rs.getTimestamp("fecha_expiracion") != null ? rs.getTimestamp("fecha_expiracion").toLocalDateTime() : null)
                    .usado(rs.getBoolean("usado"))
                    .fechaUso(rs.getTimestamp("fecha_uso") != null ? rs.getTimestamp("fecha_uso").toLocalDateTime() : null)
                    .ipSolicitud(rs.getString("ip_solicitud"))
                    .ipUso(rs.getString("ip_uso"))
                    .build());
        }, token);
    }

    public void marcarComoUsado(Long tokenId, String ipUso) {
        String sql = """
                UPDATE token_recuperacion_password
                SET usado=true, fecha_uso=NOW(), ip_uso=?
                WHERE token_id=?
                """;
        jdbcTemplate.update(sql, ipUso, tokenId);
    }

    public void eliminarExpirados() {
        String sql = "DELETE FROM token_recuperacion_password WHERE fecha_expiracion < NOW()";
        jdbcTemplate.update(sql);
    }

    public void invalidarTokensAnteriores(Long usuarioId) {
        String sql = """
                UPDATE token_recuperacion_password
                SET usado=true
                WHERE usuario_id=? AND usado=false
                """;
        jdbcTemplate.update(sql, usuarioId);
    }
}
