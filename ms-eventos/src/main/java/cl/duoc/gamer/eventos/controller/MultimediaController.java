package cl.duoc.gamer.eventos.controller;

import cl.duoc.gamer.eventos.dto.ContenidoMultimedia;
import cl.duoc.gamer.eventos.dto.MultimediaResponse;
import cl.duoc.gamer.eventos.security.UsuarioToken;
import cl.duoc.gamer.eventos.service.MultimediaService;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

// Fotos y videos de un evento. Todas las rutas son privadas: SeguridadConfig exige el token
@RestController
@RequestMapping("/api/eventos/{eventoId}/multimedia")
@RequiredArgsConstructor
public class MultimediaController {

    private final MultimediaService multimediaService;

    @GetMapping
    public List<MultimediaResponse> listar(@PathVariable Long eventoId, UsuarioToken usuario) {
        return multimediaService.listar(eventoId, usuario);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MultimediaResponse agregar(@PathVariable Long eventoId, UsuarioToken usuario,
            @RequestPart("archivo") MultipartFile archivo,
            @RequestParam(defaultValue = "")
            @Size(max = 120, message = "La descripción puede tener hasta 120 caracteres") String descripcion) {
        return multimediaService.agregar(eventoId, usuario, archivo, descripcion);
    }

    // Se devuelve como recurso para que Spring responda los rangos (Range) que piden los reproductores de video.
    // El tipo es el que se detecto al subir el archivo y la politica CSP impide que se ejecute como pagina
    @GetMapping("/{id}/contenido")
    public ResponseEntity<Resource> contenido(@PathVariable Long eventoId, @PathVariable Long id) {
        ContenidoMultimedia archivo = multimediaService.contenido(eventoId, id);
        MediaType tipo = MediaType.parseMediaType(archivo.tipoContenido());
        ContentDisposition disposicion = ContentDisposition.inline()
                .filename("multimedia-" + id + "." + tipo.getSubtype())
                .build();
        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString())
                .header("Content-Security-Policy", "default-src 'none'; sandbox")
                .cacheControl(CacheControl.noStore())
                .body(new ByteArrayResource(archivo.contenido()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long eventoId, @PathVariable Long id, UsuarioToken usuario) {
        multimediaService.eliminar(eventoId, id, usuario);
    }
}
