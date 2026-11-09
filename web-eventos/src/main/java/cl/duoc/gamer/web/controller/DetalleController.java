package cl.duoc.gamer.web.controller;

import cl.duoc.gamer.web.cliente.ClienteEventos;
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

    // Pagina privada: Spring Security redirige al login si no hay sesion
    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable Long id, @AuthenticationPrincipal UsuarioSesion usuario, Model model) {
        model.addAttribute("evento", clienteEventos.detalle(id, usuario.token()));
        return "detalle";
    }
}
