package pe.edu.utp.spa.app.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ParametroSistema {
    private Long parametroId;
    private String codigo;
    private String descripcion;
    private String valor;
    private String tipoDato;
    private String estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
}
