package cl.duoc.gamer.eventos.controller;

import cl.duoc.gamer.eventos.dto.EventoDetalleResponse;
import cl.duoc.gamer.eventos.dto.EventoResumenResponse;
import cl.duoc.gamer.eventos.dto.FiltroEventosRequest;
import cl.duoc.gamer.eventos.dto.FiltrosResponse;
import cl.duoc.gamer.eventos.dto.PaginaResponse;
import cl.duoc.gamer.eventos.service.EventoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    @GetMapping("/recientes")
    public List<EventoResumenResponse> recientes(
            @RequestParam(defaultValue = "6") @Min(value = 1, message = "El límite mínimo es 1")
            @Max(value = 12, message = "El límite máximo es 12") int limite) {
        return eventoService.recientes(limite);
    }

    @GetMapping("/populares")
    public List<EventoResumenResponse> populares(
            @RequestParam(defaultValue = "4") @Min(value = 1, message = "El límite mínimo es 1")
            @Max(value = 12, message = "El límite máximo es 12") int limite) {
        return eventoService.populares(limite);
    }

    @GetMapping("/buscar")
    public PaginaResponse<EventoResumenResponse> buscar(@Valid FiltroEventosRequest filtro,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "La página no puede ser negativa")
            @Max(value = 500, message = "La página máxima es 500") int pagina,
            @RequestParam(defaultValue = "9") @Min(value = 1, message = "El tamaño mínimo es 1")
            @Max(value = 24, message = "El tamaño máximo es 24") int tamano) {
        return eventoService.buscar(filtro, pagina, tamano);
    }

    @GetMapping("/filtros")
    public FiltrosResponse filtros() {
        return eventoService.filtros();
    }

    // Privado: SeguridadConfig exige un token valido para esta ruta
    @GetMapping("/{id}")
    public EventoDetalleResponse detalle(@PathVariable Long id) {
        return eventoService.detalle(id);
    }
}
