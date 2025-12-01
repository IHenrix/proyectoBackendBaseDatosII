package pe.edu.utp.spa.app.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.edu.utp.spa.app.model.ParametroSistema;

import java.util.List;
import java.util.Optional;

@Repository
public class ParametroSistemaDao {

    private final JdbcTemplate jdbcTemplate;

    public ParametroSistemaDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ParametroSistema> findAllActivos() {
        String sql = """
                SELECT parametro_id, codigo, descripcion, valor, tipo_dato, estado, fecha_creacion, fecha_modificacion
                FROM parametro_sistema
                WHERE estado='A'
                ORDER BY parametro_id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> map(rs));
    }

    public Optional<ParametroSistema> findById(Long id) {
        String sql = """
                SELECT parametro_id, codigo, descripcion, valor, tipo_dato, estado, fecha_creacion, fecha_modificacion
                FROM parametro_sistema
                WHERE parametro_id=?
                """;
        return jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) return Optional.empty();
            return Optional.of(map(rs));
        }, id);
    }

    public Long insert(ParametroSistema p) {
        String sql = """
                INSERT INTO parametro_sistema (codigo, descripcion, valor, tipo_dato, estado)
                VALUES (?,?,?,?,?)
                RETURNING parametro_id
                """;
        return jdbcTemplate.queryForObject(sql, Long.class,
                p.getCodigo(), p.getDescripcion(), p.getValor(), p.getTipoDato(), p.getEstado());
    }

    public void update(Long id, ParametroSistema p) {
        String sql = """
                UPDATE parametro_sistema
                SET codigo=?, descripcion=?, valor=?, tipo_dato=?, estado=?, fecha_modificacion=NOW()
                WHERE parametro_id=?
                """;
        jdbcTemplate.update(sql, p.getCodigo(), p.getDescripcion(), p.getValor(), p.getTipoDato(), p.getEstado(), id);
    }

    public int getValorInt(String codigo, int defaultValue) {
        try {
            String sql = "SELECT valor FROM parametro_sistema WHERE codigo=? AND estado='A' LIMIT 1";
            String valor = jdbcTemplate.queryForObject(sql, String.class, codigo);
            return valor != null ? Integer.parseInt(valor) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private ParametroSistema map(java.sql.ResultSet rs) throws java.sql.SQLException {
        return ParametroSistema.builder()
                .parametroId(rs.getLong("parametro_id"))
                .codigo(rs.getString("codigo"))
                .descripcion(rs.getString("descripcion"))
                .valor(rs.getString("valor"))
                .tipoDato(rs.getString("tipo_dato"))
                .estado(rs.getString("estado"))
                .fechaCreacion(rs.getTimestamp("fecha_creacion") != null ? rs.getTimestamp("fecha_creacion").toLocalDateTime() : null)
                .fechaModificacion(rs.getTimestamp("fecha_modificacion") != null ? rs.getTimestamp("fecha_modificacion").toLocalDateTime() : null)
                .build();
    }
}
