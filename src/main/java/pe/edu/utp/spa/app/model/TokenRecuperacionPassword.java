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
public class TokenRecuperacionPassword {
    private Long tokenId;
    private Long usuarioId;
    private String token;
    private String email;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaExpiracion;
    private Boolean usado;
    private LocalDateTime fechaUso;
    private String ipSolicitud;
    private String ipUso;
}
