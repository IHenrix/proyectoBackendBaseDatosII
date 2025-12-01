package pe.edu.utp.spa.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.spa.app.dto.rol.RolResponse;
import pe.edu.utp.spa.app.service.RolService;

import java.util.List;

@RestController
@RequestMapping("/admin/roles/activos")
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    public ResponseEntity<List<RolResponse>> listar() {
        return ResponseEntity.ok(rolService.listarActivos());
    }
}
