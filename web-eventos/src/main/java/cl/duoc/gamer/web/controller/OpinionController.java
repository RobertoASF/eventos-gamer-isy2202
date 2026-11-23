package cl.duoc.gamer.web.controller;

import cl.duoc.gamer.web.cliente.ClienteInteracciones;
import cl.duoc.gamer.web.cliente.MensajesApi;
import cl.duoc.gamer.web.modelo.OpinionForm;
import cl.duoc.gamer.web.seguridad.UsuarioSesion;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Comentar y valorar eventos (privado). Los formularios llevan el token CSRF que agrega Thymeleaf
@Controller
@RequiredArgsConstructor
public class OpinionController {

    private static final String VOLVER = "redirect:/detalle/{id}#opiniones";

    private final ClienteInteracciones clienteInteracciones;

    @PostMapping("/detalle/{id}/opiniones")
    public String guardar(@PathVariable Long id, @AuthenticationPrincipal UsuarioSesion usuario,
            @Valid @ModelAttribute("opinionForm") OpinionForm formulario, BindingResult errores,
            RedirectAttributes redireccion) {
        if (errores.hasErrors()) {
            redireccion.addFlashAttribute("opinionForm", formulario);
            redireccion.addFlashAttribute("alertaOpinion", primerError(errores));
            return VOLVER;
        }
        try {
            clienteInteracciones.guardarOpinion(id, usuario.token(), formulario.getPuntuacion(),
                    formulario.getTexto());
            redireccion.addFlashAttribute("avisoOpinion", "Tu opinión quedó publicada.");
        } catch (HttpClientErrorException.BadRequest ex) {
            redireccion.addFlashAttribute("opinionForm", formulario);
            redireccion.addFlashAttribute("alertaOpinion", MensajesApi.de(ex));
        }
        return VOLVER;
    }

    @PostMapping("/detalle/{id}/opiniones/{comentarioId}/eliminar")
    public String eliminar(@PathVariable Long id, @PathVariable Long comentarioId,
            @AuthenticationPrincipal UsuarioSesion usuario, RedirectAttributes redireccion) {
        try {
            clienteInteracciones.eliminarOpinion(id, comentarioId, usuario.token());
            redireccion.addFlashAttribute("avisoOpinion", "El comentario se eliminó.");
        } catch (HttpClientErrorException.Forbidden ex) {
            redireccion.addFlashAttribute("alertaOpinion", MensajesApi.de(ex));
        }
        return VOLVER;
    }

    private String primerError(BindingResult errores) {
        return errores.getFieldErrors().isEmpty() ? "Revisa los datos del formulario."
                : errores.getFieldErrors().get(0).getDefaultMessage();
    }
}
