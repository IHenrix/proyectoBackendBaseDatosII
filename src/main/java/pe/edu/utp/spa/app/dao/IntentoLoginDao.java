package pe.edu.utp.spa.app.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class IntentoLoginDao {

    private final JdbcTemplate jdbcTemplate;

    public IntentoLoginDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void registrarIntento(Long usuarioId, String username, String resultado, String mensajeError) {
        String sql = """
                INSERT INTO intento_login (usuario_id, username, resultado, mensaje_error, fecha_intento)
                VALUES (?,?,?,?, NOW())
                """;
        jdbcTemplate.update(sql, usuarioId, username, resultado, mensajeError);
    }
}
