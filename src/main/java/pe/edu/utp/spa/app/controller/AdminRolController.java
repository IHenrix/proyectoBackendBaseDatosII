package pe.edu.utp.spa.app.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.spa.app.dto.rol.PermisoDto;
import pe.edu.utp.spa.app.dto.rol.RolCreateRequest;
import pe.edu.utp.spa.app.dto.rol.RolResponse;
import pe.edu.utp.spa.app.dto.rol.RolUpdateRequest;
import pe.edu.utp.spa.app.dto.common.ApiResponse;
import pe.edu.utp.spa.app.service.RolService;

import java.util.List;

@RestController
@RequestMapping("/admin/roles")

public class AdminRolController {

    private final RolService rolService;

    public AdminRolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    public ResponseEntity<List<RolResponse>> listar() {
        return ResponseEntity.ok(rolService.listar());
    }

    @GetMapping("/permisos")
    public ResponseEntity<List<PermisoDto>> permisosActivos() {
        return ResponseEntity.ok(rolService.listarPermisosActivos());
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<RolResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(rolService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<RolResponse> crear(@Valid @RequestBody RolCreateRequest request) {
        return ResponseEntity.ok(rolService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RolResponse> actualizar(@PathVariable Long id, @Valid @RequestBody RolUpdateRequest request) {
        return ResponseEntity.ok(rolService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> desactivar(@PathVariable Long id) {
        rolService.desactivar(id);
        return ResponseEntity.ok(new ApiResponse(true, "Rol desactivado"));
    }
}
