package pe.edu.utp.spa.app.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.edu.utp.spa.app.model.Permiso;

import java.util.List;

@Repository
public class PermisoDao {

    private final JdbcTemplate jdbcTemplate;

    public PermisoDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Permiso> findByUsuario(Long usuarioId) {
        String sql = """
                SELECT p.permiso_id, p.nombre_permiso, p.descripcion, p.modulo, p.estado, p.fecha_creacion, p.fecha_modificacion
                FROM permiso p
                JOIN rol_permiso rp ON p.permiso_id = rp.permiso_id AND rp.estado='A'
                JOIN rol r ON r.rol_id = rp.rol_id AND r.estado='A'
                JOIN usuario_rol ur ON ur.rol_id = r.rol_id AND ur.estado='A'
                WHERE ur.usuario_id=? AND p.estado='A'
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapPermiso(rs), usuarioId);
    }

    public List<Permiso> findByRol(Long rolId) {
        String sql = """
                SELECT p.permiso_id, p.nombre_permiso, p.descripcion, p.modulo, p.estado, p.fecha_creacion, p.fecha_modificacion
                FROM permiso p
                JOIN rol_permiso rp ON p.permiso_id = rp.permiso_id
                WHERE rp.rol_id=? AND rp.estado='A' AND p.estado='A'
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapPermiso(rs), rolId);
    }

    public List<Permiso> findAllActivos() {
        String sql = """
                SELECT p.permiso_id, p.nombre_permiso, p.descripcion, p.modulo, p.estado, p.fecha_creacion, p.fecha_modificacion
                FROM permiso p
                WHERE p.estado='A'
                ORDER BY p.permiso_id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapPermiso(rs));
    }

    private Permiso mapPermiso(java.sql.ResultSet rs) throws java.sql.SQLException {
        return Permiso.builder()
                .permisoId(rs.getLong("permiso_id"))
                .nombrePermiso(rs.getString("nombre_permiso"))
                .descripcion(rs.getString("descripcion"))
                .modulo(rs.getString("modulo"))
                .estado(rs.getString("estado"))
                .fechaCreacion(rs.getTimestamp("fecha_creacion") != null ? rs.getTimestamp("fecha_creacion").toLocalDateTime() : null)
                .fechaModificacion(rs.getTimestamp("fecha_modificacion") != null ? rs.getTimestamp("fecha_modificacion").toLocalDateTime() : null)
                .build();
    }
}
