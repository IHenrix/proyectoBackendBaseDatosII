package pe.edu.utp.spa.app.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import pe.edu.utp.spa.app.model.Persona;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;

@Repository
public class PersonaDao {

    private final JdbcTemplate jdbcTemplate;

    public PersonaDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long insert(Persona persona) {
        String sql = """
                INSERT INTO persona (nombres, apellido_paterno, apellido_materno, tipo_documento_id, numero_documento, email, telefono, fecha_nacimiento, genero, direccion, estado)
                VALUES (?,?,?,?,?,?,?,?,?,?,?)
                RETURNING persona_id
                """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, persona.getNombres());
            ps.setString(2, persona.getApellidoPaterno());
            ps.setString(3, persona.getApellidoMaterno());
            ps.setObject(4, persona.getTipoDocumentoId());
            ps.setString(5, persona.getNumeroDocumento());
            ps.setString(6, persona.getEmail());
            ps.setString(7, persona.getTelefono());
            ps.setObject(8, persona.getFechaNacimiento() != null ? Date.valueOf(persona.getFechaNacimiento()) : null);
            ps.setString(9, persona.getGenero());
            ps.setString(10, persona.getDireccion());
            ps.setString(11, persona.getEstado());
            return ps;
        }, kh);
        Number key = kh.getKeys() != null ? (Number) kh.getKeys().get("persona_id") : kh.getKey();
        return key.longValue();
    }

    public void update(Long personaId, Persona persona) {
        String sql = """
                UPDATE persona
                SET nombres=?, apellido_paterno=?, apellido_materno=?, tipo_documento_id=?, numero_documento=?, email=?, telefono=?, fecha_nacimiento=?, genero=?, direccion=?, estado=?, fecha_modificacion=NOW()
                WHERE persona_id=?
                """;
        jdbcTemplate.update(sql,
                persona.getNombres(),
                persona.getApellidoPaterno(),
                persona.getApellidoMaterno(),
                persona.getTipoDocumentoId(),
                persona.getNumeroDocumento(),
                persona.getEmail(),
                persona.getTelefono(),
                persona.getFechaNacimiento() != null ? Date.valueOf(persona.getFechaNacimiento()) : null,
                persona.getGenero(),
                persona.getDireccion(),
                persona.getEstado(),
                personaId);
    }

    public Optional<Persona> findById(Long personaId) {
        String sql = """
                SELECT p.persona_id, p.nombres, p.apellido_paterno, p.apellido_materno, p.tipo_documento_id, p.numero_documento,
                       CASE
                           WHEN p.tipo_documento_id = 1 THEN 'DNI'
                           WHEN p.tipo_documento_id = 2 THEN 'CE'
                           WHEN p.tipo_documento_id = 3 THEN 'PASAPORTE'
                           WHEN p.tipo_documento_id = 4 THEN 'RUC'
                           ELSE 'OTROS'
                       END AS tipo_documento_nombre,
                       p.email, p.telefono, p.fecha_nacimiento, p.genero, p.direccion, p.estado, p.fecha_creacion, p.fecha_modificacion
                FROM persona p
                WHERE p.persona_id=?
                """;
        return jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) return Optional.empty();
            return Optional.of(mapPersona(rs));
        }, personaId);
    }

    public java.util.List<Persona> findByFiltros(String nombres, String apellidoPaterno, String numeroDocumento, Boolean sinUsuario) {
        StringBuilder sb = new StringBuilder("""
                SELECT p.persona_id, p.nombres, p.apellido_paterno, p.apellido_materno, p.tipo_documento_id, p.numero_documento,
                       CASE
                           WHEN p.tipo_documento_id = 1 THEN 'DNI'
                           WHEN p.tipo_documento_id = 2 THEN 'CE'
                           WHEN p.tipo_documento_id = 3 THEN 'PASAPORTE'
                           WHEN p.tipo_documento_id = 4 THEN 'RUC'
                           ELSE 'OTROS'
                       END AS tipo_documento_nombre,
                       p.email, p.telefono, p.fecha_nacimiento, p.genero, p.direccion, p.estado, p.fecha_creacion, p.fecha_modificacion
                FROM persona p
                WHERE 1=1
                """);
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (nombres != null && !nombres.isBlank()) {
            sb.append(" AND lower(p.nombres) LIKE ?");
            params.add("%" + nombres.toLowerCase() + "%");
        }
        if (apellidoPaterno != null && !apellidoPaterno.isBlank()) {
            sb.append(" AND lower(p.apellido_paterno) LIKE ?");
            params.add("%" + apellidoPaterno.toLowerCase() + "%");
        }
        if (numeroDocumento != null && !numeroDocumento.isBlank()) {
            sb.append(" AND p.numero_documento LIKE ?");
            params.add("%" + numeroDocumento + "%");
        }
        if (Boolean.TRUE.equals(sinUsuario)) {
            sb.append(" AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.persona_id = p.persona_id AND u.estado <> 'I')");
        }
        sb.append(" ORDER BY p.persona_id");
        return jdbcTemplate.query(sb.toString(), params.toArray(), (rs, rowNum) -> mapPersona(rs));
    }

    public void deactivate(Long personaId) {
        String sql = "UPDATE persona SET estado='I', fecha_modificacion=NOW() WHERE persona_id=?";
        jdbcTemplate.update(sql, personaId);
    }

    private Persona mapPersona(java.sql.ResultSet rs) throws java.sql.SQLException {
        return Persona.builder()
                .personaId(rs.getLong("persona_id"))
                .nombres(rs.getString("nombres"))
                .apellidoPaterno(rs.getString("apellido_paterno"))
                .apellidoMaterno(rs.getString("apellido_materno"))
                .tipoDocumentoId(rs.getLong("tipo_documento_id"))
                .tipoDocumentoNombre(rs.getString("tipo_documento_nombre"))
                .numeroDocumento(rs.getString("numero_documento"))
                .email(rs.getString("email"))
                .telefono(rs.getString("telefono"))
                .fechaNacimiento(rs.getDate("fecha_nacimiento") != null ? rs.getDate("fecha_nacimiento").toLocalDate() : null)
                .genero(rs.getString("genero"))
                .direccion(rs.getString("direccion"))
                .estado(rs.getString("estado"))
                .fechaCreacion(rs.getTimestamp("fecha_creacion") != null ? rs.getTimestamp("fecha_creacion").toLocalDateTime() : null)
                .fechaModificacion(rs.getTimestamp("fecha_modificacion") != null ? rs.getTimestamp("fecha_modificacion").toLocalDateTime() : null)
                .build();
    }
}
