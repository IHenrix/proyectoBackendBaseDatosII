package pe.edu.utp.spa.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Persona {
    private Long personaId;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private Long tipoDocumentoId;
    private String numeroDocumento;
    private String tipoDocumentoNombre;
    private String email;
    private String telefono;
    private LocalDate fechaNacimiento;
    private String genero;
    private String direccion;
    private String estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
}
