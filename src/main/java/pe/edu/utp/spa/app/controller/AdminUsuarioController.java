package pe.edu.utp.spa.app.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.spa.app.dto.common.ApiResponse;
import pe.edu.utp.spa.app.dto.usuario.UsuarioAccesoUpdateRequest;
import pe.edu.utp.spa.app.dto.usuario.UsuarioCreateRequest;
import pe.edu.utp.spa.app.dto.usuario.UsuarioFromPersonaRequest;
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
    public ResponseEntity<List<UsuarioResponse>> listar(
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String apellidoPaterno,
            @RequestParam(required = false) String apellidoMaterno,
            @RequestParam(required = false) String username
    ) {
        return ResponseEntity.ok(usuarioService.buscar(nombres, apellidoPaterno, apellidoMaterno, username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioCreateRequest request) {
        return ResponseEntity.ok(usuarioService.crear(request));
    }

    @PostMapping("/desde-persona")
    public ResponseEntity<UsuarioResponse> crearDesdePersona(@Valid @RequestBody UsuarioFromPersonaRequest request) {
        return ResponseEntity.ok(usuarioService.crearDesdePersona(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateRequest request) {
        return ResponseEntity.ok(usuarioService.actualizar(id, request));
    }

    @PutMapping("/{id}/acceso")
    public ResponseEntity<UsuarioResponse> actualizarAcceso(@PathVariable Long id, @Valid @RequestBody UsuarioAccesoUpdateRequest request) {
        return ResponseEntity.ok(usuarioService.actualizarAcceso(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> desactivar(@PathVariable Long id) {
        usuarioService.desactivar(id);
        return ResponseEntity.ok(new ApiResponse(true, "Usuario desactivado"));
    }

    @PostMapping("/{id}/activar")
    public ResponseEntity<ApiResponse> activar(@PathVariable Long id) {
        usuarioService.activar(id);
        return ResponseEntity.ok(new ApiResponse(true, "Usuario activado"));
    }

    @PostMapping("/{id}/desbloquear")
    public ResponseEntity<ApiResponse> desbloquearCuenta(@PathVariable Long id) {
        usuarioService.desbloquearCuenta(id);
        return ResponseEntity.ok(new ApiResponse(true, "Cuenta desbloqueada exitosamente. El usuario puede volver a iniciar sesi��n."));
    }
}
