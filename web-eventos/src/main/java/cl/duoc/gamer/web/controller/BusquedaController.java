package cl.duoc.gamer.web.controller;

import cl.duoc.gamer.web.cliente.ClienteEventos;
import cl.duoc.gamer.web.modelo.FiltroBusqueda;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class BusquedaController {

    private static final int PAGINA_MAXIMA = 500;

    private final ClienteEventos clienteEventos;

    @GetMapping("/buscar")
    public String buscar(@Valid @ModelAttribute("filtro") FiltroBusqueda filtro, BindingResult errores,
            @RequestParam(defaultValue = "0") int pagina, Model model) {
        model.addAttribute("opciones", clienteEventos.filtros());
        if (!errores.hasErrors()) {
            int paginaValida = Math.clamp(pagina, 0, PAGINA_MAXIMA);
            model.addAttribute("resultado", clienteEventos.buscar(filtro, paginaValida));
        }
        return "buscar";
    }
}
