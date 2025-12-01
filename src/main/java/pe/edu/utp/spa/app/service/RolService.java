package pe.edu.utp.spa.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.spa.app.dao.PermisoDao;
import pe.edu.utp.spa.app.dao.RolDao;
import pe.edu.utp.spa.app.dao.RolPermisoDao;
import pe.edu.utp.spa.app.dto.rol.PermisoDto;
import pe.edu.utp.spa.app.dto.rol.RolCreateRequest;
import pe.edu.utp.spa.app.dto.rol.RolResponse;
import pe.edu.utp.spa.app.dto.rol.RolUpdateRequest;
import pe.edu.utp.spa.app.exception.NotFoundException;
import pe.edu.utp.spa.app.model.Permiso;
import pe.edu.utp.spa.app.model.Rol;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RolService {

    private final RolDao rolDao;
    private final PermisoDao permisoDao;
    private final RolPermisoDao rolPermisoDao;

    public RolService(RolDao rolDao, PermisoDao permisoDao, RolPermisoDao rolPermisoDao) {
        this.rolDao = rolDao;
        this.permisoDao = permisoDao;
        this.rolPermisoDao = rolPermisoDao;
    }

    @Transactional(readOnly = true)
    public List<RolResponse> listar() {
        return rolDao.findAll().stream()
                .map(r -> {
                    List<PermisoDto> permisos = permisoDao.findByRol(r.getRolId()).stream()
                            .map(this::mapPermiso)
                            .toList();
                    return mapRol(r, permisos);
                }).toList();
    }

    @Transactional(readOnly = true)
    public List<RolResponse> listarActivos() {
        return rolDao.findAllActivos().stream()
                .map(r -> {
                    List<PermisoDto> permisos = permisoDao.findByRol(r.getRolId()).stream()
                            .map(this::mapPermiso)
                            .toList();
                    return mapRol(r, permisos);
                }).toList();
    }

    @Transactional(readOnly = true)
    public RolResponse obtener(Long rolId) {
        Rol rol = rolDao.findById(rolId).orElseThrow(() -> new NotFoundException("Rol no encontrado"));
        List<PermisoDto> permisos = permisoDao.findByRol(rolId).stream().map(this::mapPermiso).toList();
        return mapRol(rol, permisos);
    }

    @Transactional
    public RolResponse crear(RolCreateRequest request) {
        Rol rol = Rol.builder()
                .nombreRol(request.nombreRol())
                .descripcion(request.descripcion())
                .tipoRol(request.tipoRol() != null ? request.tipoRol() : "INTERNO")
                .estado("A")
                .build();
        Long rolId = rolDao.insert(rol);
        rolPermisoDao.replacePermisos(rolId, request.permisosIds());
        return obtener(rolId);
    }

    @Transactional
    public RolResponse actualizar(Long rolId, RolUpdateRequest request) {
        rolDao.findById(rolId).orElseThrow(() -> new NotFoundException("Rol no encontrado"));
        Rol rol = Rol.builder()
                .nombreRol(request.nombreRol())
                .descripcion(request.descripcion())
                .tipoRol(request.tipoRol())
                .estado(request.estado())
                .build();
        rolDao.update(rolId, rol);
        rolPermisoDao.replacePermisos(rolId, request.permisosIds());
        return obtener(rolId);
    }

    @Transactional
    public void desactivar(Long rolId) {
        Rol rol = rolDao.findById(rolId).orElseThrow(() -> new NotFoundException("Rol no encontrado"));
        Rol nuevo = Rol.builder()
                .nombreRol(rol.getNombreRol())
                .descripcion(rol.getDescripcion())
                .tipoRol(rol.getTipoRol())
                .estado("I")
                .build();
        rolDao.update(rolId, nuevo);
    }

    @Transactional(readOnly = true)
    public List<PermisoDto> listarPermisosActivos() {
        return permisoDao.findAllActivos().stream().map(this::mapPermiso).collect(Collectors.toList());
    }

    private RolResponse mapRol(Rol rol, List<PermisoDto> permisos) {
        return new RolResponse(rol.getRolId(), rol.getNombreRol(), rol.getDescripcion(), rol.getTipoRol(), rol.getEstado(), permisos);
    }

    private PermisoDto mapPermiso(Permiso p) {
        return new PermisoDto(p.getPermisoId(), p.getNombrePermiso(), p.getDescripcion(), p.getModulo());
    }
}
