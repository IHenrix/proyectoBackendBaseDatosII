package pe.edu.utp.spa.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.spa.app.dao.PersonaDao;
import pe.edu.utp.spa.app.dto.usuario.PersonaCreateRequest;
import pe.edu.utp.spa.app.dto.usuario.PersonaDto;
import pe.edu.utp.spa.app.dto.usuario.PersonaUpdateRequest;
import pe.edu.utp.spa.app.exception.NotFoundException;
import pe.edu.utp.spa.app.model.Persona;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PersonaService {

    private final PersonaDao personaDao;

    public PersonaService(PersonaDao personaDao) {
        this.personaDao = personaDao;
    }

    @Transactional
    public PersonaDto crear(PersonaCreateRequest request) {
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
        Long id = personaDao.insert(persona);
        return obtener(id);
    }

    @Transactional
    public PersonaDto actualizar(Long id, PersonaUpdateRequest request) {
        Persona existente = personaDao.findById(id).orElseThrow(() -> new NotFoundException("Persona no encontrada"));
        Persona persona = Persona.builder()
                .personaId(existente.getPersonaId())
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
        personaDao.update(id, persona);
        return obtener(id);
    }

    @Transactional(readOnly = true)
    public PersonaDto obtener(Long id) {
        Persona persona = personaDao.findById(id).orElseThrow(() -> new NotFoundException("Persona no encontrada"));
        return map(persona);
    }

    @Transactional(readOnly = true)
    public List<PersonaDto> listar(String nombres, String apellidoPaterno, String numeroDocumento, Boolean sinUsuario) {
        return personaDao.findByFiltros(nombres, apellidoPaterno, numeroDocumento, sinUsuario)
                .stream().map(this::map).collect(Collectors.toList());
    }

    @Transactional
    public void desactivar(Long id) {
        personaDao.deactivate(id);
    }

    private PersonaDto map(Persona persona) {
        return new PersonaDto(
                persona.getPersonaId(),
                persona.getNombres(),
                persona.getApellidoPaterno(),
                persona.getApellidoMaterno(),
                persona.getTipoDocumentoId(),
                persona.getNumeroDocumento(),
                persona.getEmail(),
                persona.getTelefono(),
                persona.getFechaNacimiento(),
                persona.getGenero(),
                persona.getDireccion(),
                persona.getEstado()
        );
    }
}
