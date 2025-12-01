package pe.edu.utp.spa.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.spa.app.dao.ParametroSistemaDao;
import pe.edu.utp.spa.app.dto.parametro.ParametroRequest;
import pe.edu.utp.spa.app.dto.parametro.ParametroResponse;
import pe.edu.utp.spa.app.exception.NotFoundException;
import pe.edu.utp.spa.app.model.ParametroSistema;

import java.util.List;

@Service
public class ParametroSistemaService {

    private final ParametroSistemaDao parametroSistemaDao;

    public ParametroSistemaService(ParametroSistemaDao parametroSistemaDao) {
        this.parametroSistemaDao = parametroSistemaDao;
    }

    @Transactional(readOnly = true)
    public List<ParametroResponse> listarActivos() {
        return parametroSistemaDao.findAllActivos().stream()
                .map(this::mapResponse)
                .toList();
    }

    @Transactional
    public ParametroResponse crear(ParametroRequest request) {
        ParametroSistema p = ParametroSistema.builder()
                .codigo(request.codigo())
                .descripcion(request.descripcion())
                .valor(request.valor())
                .tipoDato(request.tipoDato() != null ? request.tipoDato() : "STRING")
                .estado(request.estado() != null ? request.estado() : "A")
                .build();
        Long id = parametroSistemaDao.insert(p);
        return obtener(id);
    }

    @Transactional
    public ParametroResponse actualizar(Long id, ParametroRequest request) {
        parametroSistemaDao.findById(id).orElseThrow(() -> new NotFoundException("Parámetro no encontrado"));
        ParametroSistema p = ParametroSistema.builder()
                .codigo(request.codigo())
                .descripcion(request.descripcion())
                .valor(request.valor())
                .tipoDato(request.tipoDato() != null ? request.tipoDato() : "STRING")
                .estado(request.estado() != null ? request.estado() : "A")
                .build();
        parametroSistemaDao.update(id, p);
        return obtener(id);
    }

    @Transactional
    public void desactivar(Long id) {
        ParametroSistema existente = parametroSistemaDao.findById(id).orElseThrow(() -> new NotFoundException("Parámetro no encontrado"));
        ParametroSistema p = ParametroSistema.builder()
                .codigo(existente.getCodigo())
                .descripcion(existente.getDescripcion())
                .valor(existente.getValor())
                .tipoDato(existente.getTipoDato())
                .estado("I")
                .build();
        parametroSistemaDao.update(id, p);
    }

    @Transactional(readOnly = true)
    public ParametroResponse obtener(Long id) {
        return parametroSistemaDao.findById(id)
                .map(this::mapResponse)
                .orElseThrow(() -> new NotFoundException("Parámetro no encontrado"));
    }

    private ParametroResponse mapResponse(ParametroSistema p) {
        return new ParametroResponse(
                p.getParametroId(),
                p.getCodigo(),
                p.getDescripcion(),
                p.getValor(),
                p.getTipoDato(),
                p.getEstado()
        );
    }
}
