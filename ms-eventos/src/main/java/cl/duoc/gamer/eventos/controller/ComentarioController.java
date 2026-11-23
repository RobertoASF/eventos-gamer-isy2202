package cl.duoc.gamer.eventos.controller;

import cl.duoc.gamer.eventos.dto.ComentarioRequest;
import cl.duoc.gamer.eventos.dto.ComentarioResponse;
import cl.duoc.gamer.eventos.dto.OpinionesResponse;
import cl.duoc.gamer.eventos.security.UsuarioToken;
import cl.duoc.gamer.eventos.service.ComentarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Comentarios y valoraciones de un evento. Rutas privadas
@RestController
@RequestMapping("/api/eventos/{eventoId}/comentarios")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService comentarioService;

    @GetMapping
    public OpinionesResponse listar(@PathVariable Long eventoId, UsuarioToken usuario) {
        return comentarioService.listar(eventoId, usuario);
    }

    // 201 si es la primera opinion del usuario sobre el evento, 200 si actualizo la que tenia
    @PostMapping
    public ResponseEntity<ComentarioResponse> guardar(@PathVariable Long eventoId, UsuarioToken usuario,
            @Valid @RequestBody ComentarioRequest datos) {
        ComentarioService.Resultado resultado = comentarioService.guardar(eventoId, usuario, datos);
        return ResponseEntity.status(resultado.creado() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(resultado.comentario());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long eventoId, @PathVariable Long id, UsuarioToken usuario) {
        comentarioService.eliminar(eventoId, id, usuario);
    }
}
