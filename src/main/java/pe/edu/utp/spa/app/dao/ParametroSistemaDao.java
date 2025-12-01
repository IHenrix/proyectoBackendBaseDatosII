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

    public Optional<ParametroSistema> findByCodigo(String codigo) {
        String sql = """
                SELECT parametro_id, codigo, descripcion, valor, tipo_dato, estado,
                       fecha_creacion, fecha_modificacion
                FROM parametro_sistema
                WHERE codigo=? AND estado='A'
                """;
        return jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) return Optional.empty();
            return Optional.of(mapParametroSistema(rs));
        }, codigo);
    }

    public List<ParametroSistema> findAllActivos() {
        String sql = """
                SELECT parametro_id, codigo, descripcion, valor, tipo_dato, estado,
                       fecha_creacion, fecha_modificacion
                FROM parametro_sistema
                WHERE estado='A'
                ORDER BY codigo
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapParametroSistema(rs));
    }

    public String getValor(String codigo, String valorPorDefecto) {
        Optional<ParametroSistema> param = findByCodigo(codigo);
        return param.map(ParametroSistema::getValor).orElse(valorPorDefecto);
    }

    public Integer getValorInt(String codigo, Integer valorPorDefecto) {
        String valor = getValor(codigo, null);
        if (valor == null) return valorPorDefecto;
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return valorPorDefecto;
        }
    }

    public Boolean getValorBoolean(String codigo, Boolean valorPorDefecto) {
        String valor = getValor(codigo, null);
        if (valor == null) return valorPorDefecto;
        return "true".equalsIgnoreCase(valor) || "1".equals(valor);
    }

    private ParametroSistema mapParametroSistema(java.sql.ResultSet rs) throws java.sql.SQLException {
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
