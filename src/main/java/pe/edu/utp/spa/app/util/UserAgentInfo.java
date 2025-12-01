package pe.edu.utp.spa.app.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAgentInfo {
    private String navegador;
    private String sistemaOperativo;
    private String dispositivo;
}
