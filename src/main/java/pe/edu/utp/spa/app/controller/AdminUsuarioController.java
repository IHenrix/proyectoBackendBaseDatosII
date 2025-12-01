package pe.edu.utp.spa.app.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.spa.app.dto.common.ApiResponse;
import pe.edu.utp.spa.app.dto.usuario.UsuarioCreateRequest;
import pe.edu.utp.spa.app.dto.usuario.UsuarioResponse;
import pe.edu.utp.spa.app.dto.usuario.UsuarioUpdateRequest;
import pe.edu.utp.spa.app.service.UsuarioService;

import java.util.List;

@RestController
@RequestMapping("/admin/usuarios")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    public AdminUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listarActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioCreateRequest request) {
        return ResponseEntity.ok(usuarioService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateRequest request) {
        return ResponseEntity.ok(usuarioService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> desactivar(@PathVariable Long id) {
        usuarioService.desactivar(id);
        return ResponseEntity.ok(new ApiResponse(true, "Usuario desactivado"));
    }

    @PostMapping("/{id}/desbloquear")
    public ResponseEntity<ApiResponse> desbloquearCuenta(@PathVariable Long id) {
        usuarioService.desbloquearCuenta(id);
        return ResponseEntity.ok(new ApiResponse(true, "Cuenta desbloqueada exitosamente. El usuario puede volver a iniciar sesión."));
    }
}
