package cl.duoc.gamer.eventos.controller;

import cl.duoc.gamer.eventos.dto.CompartidoResponse;
import cl.duoc.gamer.eventos.dto.CompartirRequest;
import cl.duoc.gamer.eventos.dto.ResumenCompartidosResponse;
import cl.duoc.gamer.eventos.security.UsuarioToken;
import cl.duoc.gamer.eventos.service.CompartidoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Compartir eventos en el sitio y en redes sociales. Rutas privadas
@RestController
@RequiredArgsConstructor
public class CompartidoController {

    private final CompartidoService compartidoService;

    @GetMapping("/api/eventos/{eventoId}/compartidos")
    public ResumenCompartidosResponse resumen(@PathVariable Long eventoId, UsuarioToken usuario) {
        return compartidoService.resumen(eventoId, usuario);
    }

    @PostMapping("/api/eventos/{eventoId}/compartidos")
    public ResumenCompartidosResponse compartir(@PathVariable Long eventoId, UsuarioToken usuario,
            @Valid @RequestBody CompartirRequest datos) {
        return compartidoService.compartir(eventoId, usuario, datos);
    }

    @GetMapping("/api/compartidos")
    public List<CompartidoResponse> comunidad(
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "El límite mínimo es 1")
            @Max(value = 30, message = "El límite máximo es 30") int limite) {
        return compartidoService.comunidad(limite);
    }
}
