package pe.edu.utp.spa.app.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import pe.edu.utp.spa.app.model.Persona;
import pe.edu.utp.spa.app.model.Usuario;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioDao {

    private final JdbcTemplate jdbcTemplate;

    public UsuarioDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long insert(Usuario usuario) {
        String sql = """
                INSERT INTO usuario (persona_id, username, password_hash, tipo_usuario, intentos_fallidos, fecha_ultimo_acceso, estado)
                VALUES (?,?,?,?,0,NULL,'A')
                RETURNING usuario_id
                """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, usuario.getPersonaId());
            ps.setString(2, usuario.getUsername());
            ps.setString(3, usuario.getPasswordHash());
            ps.setString(4, usuario.getTipoUsuario());
            return ps;
        }, kh);
        Number key = kh.getKeys() != null ? (Number) kh.getKeys().get("usuario_id") : kh.getKey();
        return key.longValue();
    }

    public void update(Long usuarioId, Usuario usuario, boolean updatePassword) {
        String sql = updatePassword
                ? """
                    UPDATE usuario
                    SET username=?, password_hash=?, tipo_usuario=?, estado=?, fecha_modificacion=NOW()
                    WHERE usuario_id=?
                  """
                : """
                    UPDATE usuario
                    SET username=?, tipo_usuario=?, estado=?, fecha_modificacion=NOW()
                    WHERE usuario_id=?
                  """;
        if (updatePassword) {
            jdbcTemplate.update(sql,
                    usuario.getUsername(),
                    usuario.getPasswordHash(),
                    usuario.getTipoUsuario(),
                    usuario.getEstado(),
                    usuarioId);
        } else {
            jdbcTemplate.update(sql,
                    usuario.getUsername(),
                    usuario.getTipoUsuario(),
                    usuario.getEstado(),
                    usuarioId);
        }
    }

    public void deactivate(Long usuarioId) {
        String sql = "UPDATE usuario SET estado='I', fecha_modificacion=NOW() WHERE usuario_id=?";
        jdbcTemplate.update(sql, usuarioId);
    }

    public void actualizarIntentosFallidos(Long usuarioId, int intentos) {
        jdbcTemplate.update("UPDATE usuario SET intentos_fallidos=?, fecha_modificacion=NOW() WHERE usuario_id=?", intentos, usuarioId);
    }

    public void actualizarUltimoAcceso(Long usuarioId) {
        jdbcTemplate.update("UPDATE usuario SET fecha_ultimo_acceso=NOW(), fecha_modificacion=NOW() WHERE usuario_id=?", usuarioId);
    }

    public void bloquearCuenta(Long usuarioId) {
        String sql = "UPDATE usuario SET estado='B', fecha_modificacion=NOW() WHERE usuario_id=?";
        jdbcTemplate.update(sql, usuarioId);
    }

    public void desbloquearCuenta(Long usuarioId) {
        String sql = "UPDATE usuario SET estado='A', intentos_fallidos=0, fecha_modificacion=NOW() WHERE usuario_id=?";
        jdbcTemplate.update(sql, usuarioId);
    }

    public Optional<Usuario> findByUsername(String username) {
        String sql = """
                SELECT u.usuario_id, u.persona_id, u.username, u.password_hash, u.tipo_usuario, u.intentos_fallidos,
                       u.fecha_ultimo_acceso, u.estado, u.fecha_creacion, u.fecha_modificacion,
                       p.persona_id as p_persona_id, p.nombres, p.apellido_paterno, p.apellido_materno, p.tipo_documento_id,
                       p.numero_documento, p.email, p.telefono, p.fecha_nacimiento, p.genero, p.direccion, p.estado as p_estado,
                       p.fecha_creacion as p_fecha_creacion, p.fecha_modificacion as p_fecha_modificacion
                FROM usuario u
                JOIN persona p ON u.persona_id = p.persona_id
                WHERE u.username=?
                """;
        return jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) return Optional.empty();
            return Optional.of(mapUsuario(rs));
        }, username);
    }

    public Optional<Usuario> findActivoByUsername(String username) {
        String sql = """
                SELECT u.usuario_id, u.persona_id, u.username, u.password_hash, u.tipo_usuario, u.intentos_fallidos,
                       u.fecha_ultimo_acceso, u.estado, u.fecha_creacion, u.fecha_modificacion,
                       p.persona_id as p_persona_id, p.nombres, p.apellido_paterno, p.apellido_materno, p.tipo_documento_id,
                       p.numero_documento, p.email, p.telefono, p.fecha_nacimiento, p.genero, p.direccion, p.estado as p_estado,
                       p.fecha_creacion as p_fecha_creacion, p.fecha_modificacion as p_fecha_modificacion
                FROM usuario u
                JOIN persona p ON u.persona_id = p.persona_id
                WHERE u.username=? AND u.estado='A'
                """;
        return jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) return Optional.empty();
            return Optional.of(mapUsuario(rs));
        }, username);
    }

    public Optional<Usuario> findById(Long usuarioId) {
        String sql = """
                SELECT u.usuario_id, u.persona_id, u.username, u.password_hash, u.tipo_usuario, u.intentos_fallidos,
                       u.fecha_ultimo_acceso, u.estado, u.fecha_creacion, u.fecha_modificacion,
                       p.persona_id as p_persona_id, p.nombres, p.apellido_paterno, p.apellido_materno, p.tipo_documento_id,
                       p.numero_documento, p.email, p.telefono, p.fecha_nacimiento, p.genero, p.direccion, p.estado as p_estado,
                       p.fecha_creacion as p_fecha_creacion, p.fecha_modificacion as p_fecha_modificacion
                FROM usuario u
                JOIN persona p ON u.persona_id = p.persona_id
                WHERE u.usuario_id=?
                """;
        return jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) return Optional.empty();
            return Optional.of(mapUsuario(rs));
        }, usuarioId);
    }

    public List<Usuario> findAllActivos() {
        String sql = """
                SELECT u.usuario_id, u.persona_id, u.username, u.password_hash, u.tipo_usuario, u.intentos_fallidos,
                       u.fecha_ultimo_acceso, u.estado, u.fecha_creacion, u.fecha_modificacion,
                       p.persona_id as p_persona_id, p.nombres, p.apellido_paterno, p.apellido_materno, p.tipo_documento_id,
                       p.numero_documento, p.email, p.telefono, p.fecha_nacimiento, p.genero, p.direccion, p.estado as p_estado,
                       p.fecha_creacion as p_fecha_creacion, p.fecha_modificacion as p_fecha_modificacion
                FROM usuario u
                JOIN persona p ON u.persona_id = p.persona_id
                WHERE u.estado='A'
                ORDER BY u.usuario_id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapUsuario(rs));
    }

    public boolean existsByUsername(String username, Long excludeId) {
        String sql = excludeId == null
                ? "SELECT COUNT(1) FROM usuario WHERE username=?"
                : "SELECT COUNT(1) FROM usuario WHERE username=? AND usuario_id<>?";
        Integer count = excludeId == null
                ? jdbcTemplate.queryForObject(sql, Integer.class, username)
                : jdbcTemplate.queryForObject(sql, Integer.class, username, excludeId);
        return count != null && count > 0;
    }

    public boolean existsByPersonaDocumento(Long tipoDocumentoId, String numeroDocumento, Long excludePersonaId) {
        String sql = excludePersonaId == null
                ? "SELECT COUNT(1) FROM persona WHERE tipo_documento_id=? AND numero_documento=?"
                : "SELECT COUNT(1) FROM persona WHERE tipo_documento_id=? AND numero_documento=? AND persona_id<>?";
        Integer count = excludePersonaId == null
                ? jdbcTemplate.queryForObject(sql, Integer.class, tipoDocumentoId, numeroDocumento)
                : jdbcTemplate.queryForObject(sql, Integer.class, tipoDocumentoId, numeroDocumento, excludePersonaId);
        return count != null && count > 0;
    }

    public boolean existsByEmail(String email, Long excludePersonaId) {
        String sql = excludePersonaId == null
                ? "SELECT COUNT(1) FROM persona WHERE email=?"
                : "SELECT COUNT(1) FROM persona WHERE email=? AND persona_id<>?";
        Integer count = excludePersonaId == null
                ? jdbcTemplate.queryForObject(sql, Integer.class, email)
                : jdbcTemplate.queryForObject(sql, Integer.class, email, excludePersonaId);
        return count != null && count > 0;
    }

    private Usuario mapUsuario(java.sql.ResultSet rs) throws java.sql.SQLException {
        Persona persona = Persona.builder()
                .personaId(rs.getLong("p_persona_id"))
                .nombres(rs.getString("nombres"))
                .apellidoPaterno(rs.getString("apellido_paterno"))
                .apellidoMaterno(rs.getString("apellido_materno"))
                .tipoDocumentoId(rs.getLong("tipo_documento_id"))
                .numeroDocumento(rs.getString("numero_documento"))
                .email(rs.getString("email"))
                .telefono(rs.getString("telefono"))
                .fechaNacimiento(rs.getDate("fecha_nacimiento") != null ? rs.getDate("fecha_nacimiento").toLocalDate() : null)
                .genero(rs.getString("genero"))
                .direccion(rs.getString("direccion"))
                .estado(rs.getString("p_estado"))
                .fechaCreacion(rs.getTimestamp("p_fecha_creacion") != null ? rs.getTimestamp("p_fecha_creacion").toLocalDateTime() : null)
                .fechaModificacion(rs.getTimestamp("p_fecha_modificacion") != null ? rs.getTimestamp("p_fecha_modificacion").toLocalDateTime() : null)
                .build();

        return Usuario.builder()
                .usuarioId(rs.getLong("usuario_id"))
                .personaId(rs.getLong("persona_id"))
                .username(rs.getString("username"))
                .passwordHash(rs.getString("password_hash"))
                .tipoUsuario(rs.getString("tipo_usuario"))
                .intentosFallidos(rs.getInt("intentos_fallidos"))
                .fechaUltimoAcceso(rs.getTimestamp("fecha_ultimo_acceso") != null ? rs.getTimestamp("fecha_ultimo_acceso").toLocalDateTime() : null)
                .estado(rs.getString("estado"))
                .fechaCreacion(rs.getTimestamp("fecha_creacion") != null ? rs.getTimestamp("fecha_creacion").toLocalDateTime() : null)
                .fechaModificacion(rs.getTimestamp("fecha_modificacion") != null ? rs.getTimestamp("fecha_modificacion").toLocalDateTime() : null)
                .persona(persona)
                .build();
    }
}
