package cl.duoc.gamer.web.controller;

import cl.duoc.gamer.web.cliente.ClienteInteracciones;
import cl.duoc.gamer.web.cliente.MensajesApi;
import cl.duoc.gamer.web.modelo.CompartirForm;
import cl.duoc.gamer.web.modelo.RedSocial;
import cl.duoc.gamer.web.seguridad.UsuarioSesion;
import jakarta.validation.Valid;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Compartir eventos en el sitio (pagina Comunidad) y registrar cuando se comparten en redes sociales
@Controller
@RequiredArgsConstructor
public class CompartirController {

    private static final String VOLVER = "redirect:/detalle/{id}#compartir";

    private final ClienteInteracciones clienteInteracciones;

    @PostMapping("/detalle/{id}/compartir")
    public String compartirEnSitio(@PathVariable Long id, @AuthenticationPrincipal UsuarioSesion usuario,
            @Valid @ModelAttribute("compartirForm") CompartirForm formulario, BindingResult errores,
            RedirectAttributes redireccion) {
        if (errores.hasErrors()) {
            redireccion.addFlashAttribute("compartirForm", formulario);
            redireccion.addFlashAttribute("alertaCompartir", errores.getFieldErrors().get(0).getDefaultMessage());
            return VOLVER;
        }
        try {
            clienteInteracciones.compartir(id, usuario.token(), "SITIO", formulario.getMensaje());
            redireccion.addFlashAttribute("avisoCompartir", "Compartiste el evento en la página Comunidad.");
        } catch (HttpClientErrorException.BadRequest ex) {
            redireccion.addFlashAttribute("alertaCompartir", MensajesApi.de(ex));
        }
        return VOLVER;
    }

    // Lo llama detalle.js cuando el usuario abre uno de los enlaces de redes sociales. Lleva el token CSRF
    // en un encabezado y la red se valida contra una lista cerrada
    @PostMapping("/detalle/{id}/compartir/{red}")
    public ResponseEntity<Void> registrarRed(@PathVariable Long id, @PathVariable String red,
            @AuthenticationPrincipal UsuarioSesion usuario) {
        Optional<RedSocial> redSocial = RedSocial.buscar(red);
        if (redSocial.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        clienteInteracciones.compartir(id, usuario.token(), redSocial.get().name(), null);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/comunidad")
    public String comunidad(@AuthenticationPrincipal UsuarioSesion usuario, Model model) {
        model.addAttribute("compartidos", clienteInteracciones.comunidad(usuario.token()));
        return "comunidad";
    }
}
