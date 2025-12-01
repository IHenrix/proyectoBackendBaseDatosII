package pe.edu.utp.spa.app.service;

import org.springframework.stereotype.Service;
import pe.edu.utp.spa.app.dao.RolDao;
import pe.edu.utp.spa.app.dto.rol.RolResponse;
import pe.edu.utp.spa.app.model.Rol;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RolService {

    private final RolDao rolDao;

    public RolService(RolDao rolDao) {
        this.rolDao = rolDao;
    }

    public List<RolResponse> listarActivos() {
        List<Rol> roles = rolDao.findAllActivos();
        return roles.stream()
                .map(r -> new RolResponse(r.getRolId(), r.getNombreRol(), r.getDescripcion(), r.getTipoRol(), r.getEstado()))
                .collect(Collectors.toList());
    }
}
