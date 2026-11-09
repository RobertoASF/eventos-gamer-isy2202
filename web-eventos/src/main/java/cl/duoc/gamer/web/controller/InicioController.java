package cl.duoc.gamer.web.controller;

import cl.duoc.gamer.web.cliente.ClienteEventos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class InicioController {

    private final ClienteEventos clienteEventos;

    @GetMapping({"/", "/inicio"})
    public String inicio(Model model) {
        model.addAttribute("banners", clienteEventos.banners());
        model.addAttribute("recientes", clienteEventos.recientes());
        model.addAttribute("populares", clienteEventos.populares());
        return "inicio";
    }
}
