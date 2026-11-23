package cl.duoc.gamer.web.controller;

import cl.duoc.gamer.web.cliente.ClienteEventos;
import cl.duoc.gamer.web.cliente.ClienteInteracciones;
import cl.duoc.gamer.web.cliente.dto.Comentario;
import cl.duoc.gamer.web.cliente.dto.EventoDetalle;
import cl.duoc.gamer.web.cliente.dto.Opiniones;
import cl.duoc.gamer.web.modelo.CompartirForm;
import cl.duoc.gamer.web.modelo.EnlacesCompartir;
import cl.duoc.gamer.web.modelo.OpinionForm;
import cl.duoc.gamer.web.seguridad.UsuarioSesion;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class DetalleController {

    private final ClienteEventos clienteEventos;
    private final ClienteInteracciones clienteInteracciones;
    private final EnlacesCompartir enlacesCompartir;

    // Pagina privada: Spring Security redirige al login si no hay sesion
    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable Long id, @AuthenticationPrincipal UsuarioSesion usuario, Model model) {
        EventoDetalle evento = clienteEventos.detalle(id, usuario.token());
        Opiniones opiniones = clienteInteracciones.opiniones(id, usuario.token());

        model.addAttribute("evento", evento);
        model.addAttribute("multimedia", clienteInteracciones.multimedia(id, usuario.token()));
        model.addAttribute("opiniones", opiniones);
        model.addAttribute("compartidos", clienteInteracciones.compartidos(id, usuario.token()));
        model.addAttribute("urlEvento", enlacesCompartir.urlEvento(id));
        model.addAttribute("redes", enlacesCompartir.enlaces(id, evento.nombre()));
        if (!model.containsAttribute("opinionForm")) {
            Comentario mia = opiniones.miOpinion();
            model.addAttribute("opinionForm",
                    mia == null ? new OpinionForm() : new OpinionForm(mia.puntuacion(), mia.texto()));
        }
        if (!model.containsAttribute("compartirForm")) {
            model.addAttribute("compartirForm", new CompartirForm());
        }
        return "detalle";
    }
}
