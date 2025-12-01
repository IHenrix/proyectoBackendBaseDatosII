package pe.edu.utp.spa.app.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.spa.app.dto.common.ApiResponse;
import pe.edu.utp.spa.app.dto.parametro.ParametroRequest;
import pe.edu.utp.spa.app.dto.parametro.ParametroResponse;
import pe.edu.utp.spa.app.service.ParametroSistemaService;

import java.util.List;

@RestController
@RequestMapping("/admin/parametros")
public class ParametroSistemaController {

    private final ParametroSistemaService parametroSistemaService;

    public ParametroSistemaController(ParametroSistemaService parametroSistemaService) {
        this.parametroSistemaService = parametroSistemaService;
    }

    @GetMapping
    public ResponseEntity<List<ParametroResponse>> listar() {
        return ResponseEntity.ok(parametroSistemaService.listarActivos());
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ParametroResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(parametroSistemaService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<ParametroResponse> crear(@Valid @RequestBody ParametroRequest request) {
        return ResponseEntity.ok(parametroSistemaService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParametroResponse> actualizar(@PathVariable Long id, @Valid @RequestBody ParametroRequest request) {
        return ResponseEntity.ok(parametroSistemaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> desactivar(@PathVariable Long id) {
        parametroSistemaService.desactivar(id);
        return ResponseEntity.ok(new ApiResponse(true, "Parámetro desactivado"));
    }
}
