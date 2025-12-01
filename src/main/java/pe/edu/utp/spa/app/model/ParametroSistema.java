package pe.edu.utp.spa.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParametroSistema {
    private Long parametroId;
    private String codigo;
    private String descripcion;
    private String valor;
    private String tipoDato; // INTEGER, STRING, BOOLEAN, DECIMAL
    private String estado; // A=Activo, I=Inactivo
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
}
