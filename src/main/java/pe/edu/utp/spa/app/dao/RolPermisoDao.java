package pe.edu.utp.spa.app.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RolPermisoDao {

    private final JdbcTemplate jdbcTemplate;

    public RolPermisoDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void replacePermisos(Long rolId, List<Long> permisoIds) {
        jdbcTemplate.update("DELETE FROM rol_permiso WHERE rol_id=?", rolId);
        if (permisoIds == null || permisoIds.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO rol_permiso (rol_id, permiso_id, estado) VALUES (?,?, 'A')";
        permisoIds.forEach(pid -> jdbcTemplate.update(sql, rolId, pid));
    }
}
