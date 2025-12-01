package pe.edu.utp.spa.app.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.edu.utp.spa.app.model.Rol;

import java.util.List;

@Repository
public class RolDao {

    private final JdbcTemplate jdbcTemplate;

    public RolDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Rol> findByUsuario(Long usuarioId) {
        String sql = """
                SELECT r.rol_id, r.nombre_rol, r.descripcion, r.tipo_rol, r.estado, r.fecha_creacion, r.fecha_modificacion
                FROM rol r
                JOIN usuario_rol ur ON r.rol_id = ur.rol_id
                WHERE ur.usuario_id=? AND ur.estado='A' AND r.estado='A'
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRol(rs), usuarioId);
    }

    public List<Rol> findAllActivos() {
        String sql = "SELECT rol_id, nombre_rol, descripcion, tipo_rol, estado, fecha_creacion, fecha_modificacion FROM rol WHERE estado='A' ORDER BY rol_id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRol(rs));
    }

    public List<Rol> findAll() {
        String sql = "SELECT rol_id, nombre_rol, descripcion, tipo_rol, estado, fecha_creacion, fecha_modificacion FROM rol ORDER BY rol_id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRol(rs));
    }

    public Long insert(Rol rol) {
        String sql = """
                INSERT INTO rol (nombre_rol, descripcion, tipo_rol, estado)
                VALUES (?,?,?,?)
                RETURNING rol_id
                """;
        Long id = jdbcTemplate.queryForObject(sql, Long.class,
                rol.getNombreRol(),
                rol.getDescripcion(),
                rol.getTipoRol(),
                rol.getEstado());
        return id;
    }

    public void update(Long rolId, Rol rol) {
        String sql = """
                UPDATE rol
                SET nombre_rol=?, descripcion=?, tipo_rol=?, estado=?, fecha_modificacion=NOW()
                WHERE rol_id=?
                """;
        jdbcTemplate.update(sql,
                rol.getNombreRol(),
                rol.getDescripcion(),
                rol.getTipoRol(),
                rol.getEstado(),
                rolId);
    }

    public java.util.Optional<Rol> findById(Long rolId) {
        String sql = "SELECT rol_id, nombre_rol, descripcion, tipo_rol, estado, fecha_creacion, fecha_modificacion FROM rol WHERE rol_id=?";
        return jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) return java.util.Optional.empty();
            return java.util.Optional.of(mapRol(rs));
        }, rolId);
    }

    private Rol mapRol(java.sql.ResultSet rs) throws java.sql.SQLException {
        return Rol.builder()
                .rolId(rs.getLong("rol_id"))
                .nombreRol(rs.getString("nombre_rol"))
                .descripcion(rs.getString("descripcion"))
                .tipoRol(rs.getString("tipo_rol"))
                .estado(rs.getString("estado"))
                .fechaCreacion(rs.getTimestamp("fecha_creacion") != null ? rs.getTimestamp("fecha_creacion").toLocalDateTime() : null)
                .fechaModificacion(rs.getTimestamp("fecha_modificacion") != null ? rs.getTimestamp("fecha_modificacion").toLocalDateTime() : null)
                .build();
    }
}
