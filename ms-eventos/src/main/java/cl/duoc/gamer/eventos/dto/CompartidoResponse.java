package cl.duoc.gamer.eventos.dto;

import cl.duoc.gamer.eventos.model.Compartido;
import cl.duoc.gamer.eventos.model.DestinoCompartido;
import java.time.LocalDateTime;

public record CompartidoResponse(
        Long id,
        String autor,
        DestinoCompartido destino,
        String mensaje,
        LocalDateTime fecha,
        EventoResumenResponse evento) {

    public static CompartidoResponse desde(Compartido compartido) {
        return new CompartidoResponse(compartido.getId(), compartido.getAutor(), compartido.getDestino(),
                compartido.getMensaje(), compartido.getFechaCreacion(),
                EventoResumenResponse.desde(compartido.getEvento()));
    }
}
