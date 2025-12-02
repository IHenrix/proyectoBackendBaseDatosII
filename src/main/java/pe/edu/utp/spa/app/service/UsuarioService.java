package pe.edu.utp.spa.app.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.spa.app.dao.PersonaDao;
import pe.edu.utp.spa.app.dao.RolDao;
import pe.edu.utp.spa.app.dao.UsuarioDao;
import pe.edu.utp.spa.app.dao.UsuarioRolDao;
import pe.edu.utp.spa.app.dto.usuario.PersonaDto;
import pe.edu.utp.spa.app.dto.usuario.UsuarioCreateRequest;
import pe.edu.utp.spa.app.dto.usuario.UsuarioFromPersonaRequest;
import pe.edu.utp.spa.app.dto.usuario.UsuarioResponse;
import pe.edu.utp.spa.app.dto.usuario.UsuarioUpdateRequest;
import pe.edu.utp.spa.app.dto.usuario.UsuarioAccesoUpdateRequest;
import pe.edu.utp.spa.app.exception.BadRequestException;
import pe.edu.utp.spa.app.exception.NotFoundException;
import pe.edu.utp.spa.app.model.Persona;
import pe.edu.utp.spa.app.model.Usuario;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final PersonaDao personaDao;
    private final UsuarioDao usuarioDao;
    private final UsuarioRolDao usuarioRolDao;
    private final RolDao rolDao;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(PersonaDao personaDao, UsuarioDao usuarioDao, UsuarioRolDao usuarioRolDao, RolDao rolDao, PasswordEncoder passwordEncoder) {
        this.personaDao = personaDao;
        this.usuarioDao = usuarioDao;
        this.usuarioRolDao = usuarioRolDao;
        this.rolDao = rolDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse crear(UsuarioCreateRequest request) {
        validarUnicidad(request.username(), null, request.tipoDocumentoId(), request.numeroDocumento(), request.email(), null);

        Persona persona = Persona.builder()
                .nombres(request.nombres())
                .apellidoPaterno(request.apellidoPaterno())
                .apellidoMaterno(request.apellidoMaterno())
                .tipoDocumentoId(request.tipoDocumentoId())
                .numeroDocumento(request.numeroDocumento())
                .email(request.email())
                .telefono(request.telefono())
                .fechaNacimiento(request.fechaNacimiento())
                .genero(request.genero())
                .direccion(request.direccion())
                .estado("A")
                .build();
        Long personaId = personaDao.insert(persona);

        Usuario usuario = Usuario.builder()
                .personaId(personaId)
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .tipoUsuario(request.tipoUsuario())
                .estado("A")
                .build();

        Long usuarioId = usuarioDao.insert(usuario);
        usuarioRolDao.replaceRoles(usuarioId, request.rolesIds());

        Usuario creado = usuarioDao.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado luego de crear"));

        creado.setRoles(rolDao.findByUsuario(usuarioId));
        return mapResponse(creado);
    }

    @Transactional
    public UsuarioResponse crearDesdePersona(UsuarioFromPersonaRequest request) {
        Persona persona = personaDao.findById(request.personaId())
                .orElseThrow(() -> new NotFoundException("Persona no encontrada"));
        if (usuarioDao.existsByPersonaId(persona.getPersonaId(), null)) {
            throw new BadRequestException("La persona ya tiene un usuario asignado");
        }
        validarUnicidad(request.username(), null, persona.getTipoDocumentoId(), persona.getNumeroDocumento(), persona.getEmail(), persona.getPersonaId());

        Usuario usuario = Usuario.builder()
                .personaId(persona.getPersonaId())
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .tipoUsuario(request.tipoUsuario())
                .estado("A")
                .build();
        Long usuarioId = usuarioDao.insert(usuario);
        usuarioRolDao.replaceRoles(usuarioId, request.rolesIds());

        Usuario creado = usuarioDao.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado luego de crear"));
        creado.setRoles(rolDao.findByUsuario(usuarioId));
        return mapResponse(creado);
    }

    @Transactional
    public UsuarioResponse actualizar(Long usuarioId, UsuarioUpdateRequest request) {
        Usuario existente = usuarioDao.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        validarUnicidad(request.username(), usuarioId, request.tipoDocumentoId(), request.numeroDocumento(), request.email(), existente.getPersonaId());

        Persona persona = Persona.builder()
                .nombres(request.nombres())
                .apellidoPaterno(request.apellidoPaterno())
                .apellidoMaterno(request.apellidoMaterno())
                .tipoDocumentoId(request.tipoDocumentoId())
                .numeroDocumento(request.numeroDocumento())
                .email(request.email())
                .telefono(request.telefono())
                .fechaNacimiento(request.fechaNacimiento())
                .genero(request.genero())
                .direccion(request.direccion())
                .estado(request.estado())
                .build();
        personaDao.update(existente.getPersonaId(), persona);

        Usuario usuario = Usuario.builder()
                .username(request.username())
                .passwordHash(request.password() != null && !request.password().isBlank()
                        ? passwordEncoder.encode(request.password())
                        : existente.getPasswordHash())
                .tipoUsuario(request.tipoUsuario())
                .estado(request.estado())
                .build();
        boolean updatePassword = request.password() != null && !request.password().isBlank();
        usuarioDao.update(usuarioId, usuario, updatePassword);
        usuarioRolDao.replaceRoles(usuarioId, request.rolesIds());

        Usuario actualizado = usuarioDao.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado luego de actualizar"));
        actualizado.setRoles(rolDao.findByUsuario(usuarioId));
        return mapResponse(actualizado);
    }

    @Transactional
    public UsuarioResponse actualizarAcceso(Long usuarioId, UsuarioAccesoUpdateRequest request) {
        Usuario existente = usuarioDao.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (usuarioDao.existsByUsername(request.username(), usuarioId)) {
            throw new BadRequestException("El username ya existe");
        }
        if (request.rolesIds() == null || request.rolesIds().isEmpty()) {
            throw new BadRequestException("Debe asignar al menos un rol");
        }

        Usuario usuario = Usuario.builder()
                .username(request.username())
                .passwordHash(request.password() != null && !request.password().isBlank()
                        ? passwordEncoder.encode(request.password())
                        : existente.getPasswordHash())
                .tipoUsuario(request.tipoUsuario())
                .estado(request.estado() != null && !request.estado().isBlank() ? request.estado() : existente.getEstado())
                .build();
        boolean updatePassword = request.password() != null && !request.password().isBlank();
        usuarioDao.update(usuarioId, usuario, updatePassword);
        usuarioRolDao.replaceRoles(usuarioId, request.rolesIds());

        Usuario actualizado = usuarioDao.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado luego de actualizar"));
        actualizado.setRoles(rolDao.findByUsuario(usuarioId));
        return mapResponse(actualizado);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> buscar(String nombres, String apellidoPaterno, String apellidoMaterno, String username) {
        List<Usuario> usuarios = usuarioDao.findByFiltros(nombres, apellidoPaterno, apellidoMaterno, username);
        return usuarios.stream()
                .map(u -> {
                    u.setRoles(rolDao.findByUsuario(u.getUsuarioId()));
                    return mapResponse(u);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long usuarioId) {
        Usuario usuario = usuarioDao.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        usuario.setRoles(rolDao.findByUsuario(usuarioId));
        return mapResponse(usuario);
    }

    @Transactional
    public void desactivar(Long usuarioId) {
        Usuario usuario = usuarioDao.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        usuarioDao.deactivate(usuarioId);
        personaDao.deactivate(usuario.getPersonaId());
    }

    @Transactional
    public void activar(Long usuarioId) {
        Usuario usuario = usuarioDao.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        usuarioDao.activarCuenta(usuarioId);
        usuarioDao.actualizarIntentosFallidos(usuarioId, 0);
    }

    @Transactional
    public void desbloquearCuenta(Long usuarioId) {
        Usuario usuario = usuarioDao.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        if (!"B".equals(usuario.getEstado())) {
            throw new BadRequestException("La cuenta no está bloqueada");
        }
        usuarioDao.desbloquearCuenta(usuarioId);
    }

    private void validarUnicidad(String username, Long usuarioId, Long tipoDoc, String nroDoc, String email, Long personaId) {
        if (usuarioDao.existsByUsername(username, usuarioId)) {
            throw new BadRequestException("El username ya existe");
        }
        if (usuarioDao.existsByPersonaDocumento(tipoDoc, nroDoc, personaId)) {
            throw new BadRequestException("El documento ya existe");
        }
        if (usuarioDao.existsByEmail(email, personaId)) {
            throw new BadRequestException("El email ya existe");
        }
        if (personaId != null && usuarioDao.existsByPersonaId(personaId, usuarioId)) {
            throw new BadRequestException("La persona ya tiene un usuario asignado");
        }
    }

    private UsuarioResponse mapResponse(Usuario usuario) {
        PersonaDto personaDto = new PersonaDto(
                usuario.getPersona().getPersonaId(),
                usuario.getPersona().getNombres(),
                usuario.getPersona().getApellidoPaterno(),
                usuario.getPersona().getApellidoMaterno(),
                usuario.getPersona().getTipoDocumentoId(),
                usuario.getPersona().getTipoDocumentoNombre(),
                usuario.getPersona().getNumeroDocumento(),
                usuario.getPersona().getEmail(),
                usuario.getPersona().getTelefono(),
                usuario.getPersona().getFechaNacimiento(),
                usuario.getPersona().getGenero(),
                usuario.getPersona().getDireccion(),
                usuario.getPersona().getEstado()
        );
        List<String> roles = usuario.getRoles() == null ? List.of() :
                usuario.getRoles().stream().map(r -> r.getNombreRol()).toList();
        return new UsuarioResponse(usuario.getUsuarioId(), usuario.getUsername(), usuario.getTipoUsuario(), usuario.getEstado(), personaDto, roles);
    }
}
