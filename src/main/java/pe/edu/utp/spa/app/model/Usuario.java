package pe.edu.utp.spa.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    private Long usuarioId;
    private Long personaId;
    private String username;
    private String passwordHash;
    private String tipoUsuario;
    private Integer intentosFallidos;
    private LocalDateTime fechaUltimoAcceso;
    private String estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private Persona persona;
    private List<Rol> roles;
    private List<Permiso> permisos;
}
