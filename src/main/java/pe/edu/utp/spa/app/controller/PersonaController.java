package pe.edu.utp.spa.app.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.spa.app.dto.common.ApiResponse;
import pe.edu.utp.spa.app.dto.usuario.PersonaCreateRequest;
import pe.edu.utp.spa.app.dto.usuario.PersonaDto;
import pe.edu.utp.spa.app.dto.usuario.PersonaUpdateRequest;
import pe.edu.utp.spa.app.service.PersonaService;

import java.util.List;

@RestController
@RequestMapping("/admin/personas")
public class PersonaController {

    private final PersonaService personaService;

    public PersonaController(PersonaService personaService) {
        this.personaService = personaService;
    }

    @GetMapping
    public ResponseEntity<List<PersonaDto>> listar(
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String apellidoPaterno,
            @RequestParam(required = false) String numeroDocumento,
            @RequestParam(required = false) Boolean sinUsuario
    ) {
        return ResponseEntity.ok(personaService.listar(nombres, apellidoPaterno, numeroDocumento, sinUsuario));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonaDto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(personaService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<PersonaDto> crear(@Valid @RequestBody PersonaCreateRequest request) {
        return ResponseEntity.ok(personaService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonaDto> actualizar(@PathVariable Long id, @Valid @RequestBody PersonaUpdateRequest request) {
        return ResponseEntity.ok(personaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> desactivar(@PathVariable Long id) {
        personaService.desactivar(id);
        return ResponseEntity.ok(new ApiResponse(true, "Persona desactivada"));
    }
}
