package pe.edu.utp.spa.app.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class IntentoLoginDao {

    private final JdbcTemplate jdbcTemplate;

    public IntentoLoginDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void registrarIntento(
            Long usuarioId,
            String username,
            String resultado,
            String mensajeError,
            String ipAddress,
            String userAgent,
            String navegador,
            String sistemaOperativo,
            String dispositivo,
            String pais,
            String ciudad
    ) {
        String sql = """
                INSERT INTO intento_login
                (usuario_id, username, resultado, mensaje_error, ip_address, user_agent,
                 navegador, sistema_operativo, dispositivo, pais, ciudad, fecha_intento)
                VALUES (?,?,?,?,?,?,?,?,?,?,?, NOW())
                """;
        jdbcTemplate.update(sql,
                usuarioId,
                username,
                resultado,
                mensajeError,
                ipAddress,
                userAgent,
                navegador,
                sistemaOperativo,
                dispositivo,
                pais,
                ciudad
        );
    }
}
