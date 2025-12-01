package pe.edu.utp.spa.app.dao;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
public class UsuarioRolDao {

    private final JdbcTemplate jdbcTemplate;

    public UsuarioRolDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void replaceRoles(Long usuarioId, List<Long> rolesIds) {
        jdbcTemplate.update("DELETE FROM usuario_rol WHERE usuario_id=?", usuarioId);
        if (rolesIds == null || rolesIds.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO usuario_rol (usuario_id, rol_id, estado) VALUES (?,?, 'A')";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, usuarioId);
                ps.setLong(2, rolesIds.get(i));
            }

            @Override
            public int getBatchSize() {
                return rolesIds.size();
            }
        });
    }
}
