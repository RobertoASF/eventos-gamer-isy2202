package cl.duoc.gamer.web.controller;

import cl.duoc.gamer.web.cliente.ClienteInteracciones;
import cl.duoc.gamer.web.cliente.MensajesApi;
import cl.duoc.gamer.web.seguridad.UsuarioSesion;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Agregar fotos y videos a los eventos (privado). El archivo se revisa en ms-eventos, que es quien lo guarda
@Slf4j
@Controller
@RequiredArgsConstructor
public class MultimediaController {

    private static final String VOLVER = "redirect:/detalle/{id}#multimedia";
    private static final String ALERTA = "alertaMultimedia";

    // Solo un rango simple de bytes, como el que piden los reproductores de video
    private static final Pattern RANGO_VALIDO = Pattern.compile("bytes=\\d{0,12}-\\d{0,12}");

    // Encabezados de la respuesta de la API que se entregan al navegador
    private static final List<String> CABECERAS_ARCHIVO = List.of(HttpHeaders.CONTENT_TYPE,
            HttpHeaders.CONTENT_RANGE, HttpHeaders.ACCEPT_RANGES, HttpHeaders.CONTENT_DISPOSITION);

    private final ClienteInteracciones clienteInteracciones;

    @PostMapping("/detalle/{id}/multimedia")
    public String agregar(@PathVariable Long id, @AuthenticationPrincipal UsuarioSesion usuario,
            @RequestParam(name = "archivo", required = false) MultipartFile archivo,
            @RequestParam(name = "descripcion", defaultValue = "") String descripcion,
            RedirectAttributes redireccion) {
        if (archivo == null || archivo.isEmpty()) {
            redireccion.addFlashAttribute(ALERTA, "Elige una foto o un video para subir.");
            return VOLVER;
        }
        if (descripcion.length() > 120) {
            redireccion.addFlashAttribute(ALERTA, "La descripción puede tener hasta 120 caracteres.");
            return VOLVER;
        }
        try {
            clienteInteracciones.subirMultimedia(id, usuario.token(), archivo, descripcion);
            redireccion.addFlashAttribute("avisoMultimedia", "El archivo se agregó al evento.");
        } catch (HttpClientErrorException ex) {
            // 400: el archivo no cumple las reglas; 413: supera el tamaño. Otros errores los maneja ManejadorErroresWeb
            if (ex.getStatusCode().value() != 400 && ex.getStatusCode().value() != 413) {
                throw ex;
            }
            redireccion.addFlashAttribute(ALERTA, MensajesApi.de(ex));
        } catch (IOException ex) {
            log.warn("No se pudo leer el archivo enviado por el usuario {}", usuario.nombreUsuario());
            redireccion.addFlashAttribute(ALERTA, "No se pudo leer el archivo. Intenta de nuevo.");
        }
        return VOLVER;
    }

    @GetMapping("/detalle/{id}/multimedia/{multimediaId}")
    public ResponseEntity<byte[]> contenido(@PathVariable Long id, @PathVariable Long multimediaId,
            @RequestHeader(name = HttpHeaders.RANGE, required = false) String rango,
            @AuthenticationPrincipal UsuarioSesion usuario) {
        String rangoPermitido = rango != null && RANGO_VALIDO.matcher(rango).matches() ? rango : null;
        ResponseEntity<byte[]> respuesta = clienteInteracciones.contenidoMultimedia(id, multimediaId,
                usuario.token(), rangoPermitido);
        HttpHeaders cabeceras = new HttpHeaders();
        CABECERAS_ARCHIVO.forEach(nombre -> {
            String valor = respuesta.getHeaders().getFirst(nombre);
            if (valor != null) {
                cabeceras.set(nombre, valor);
            }
        });
        // Si alguien abre el archivo directo en el navegador, no se puede ejecutar como una pagina
        cabeceras.set("Content-Security-Policy", "default-src 'none'; sandbox");
        return new ResponseEntity<>(respuesta.getBody(), cabeceras, respuesta.getStatusCode());
    }

    @PostMapping("/detalle/{id}/multimedia/{multimediaId}/eliminar")
    public String eliminar(@PathVariable Long id, @PathVariable Long multimediaId,
            @AuthenticationPrincipal UsuarioSesion usuario, RedirectAttributes redireccion) {
        try {
            clienteInteracciones.eliminarMultimedia(id, multimediaId, usuario.token());
            redireccion.addFlashAttribute("avisoMultimedia", "El archivo se eliminó.");
        } catch (HttpClientErrorException.Forbidden ex) {
            redireccion.addFlashAttribute(ALERTA, MensajesApi.de(ex));
        }
        return VOLVER;
    }
}
