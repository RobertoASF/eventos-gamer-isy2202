package cl.duoc.gamer.eventos.dto;

import cl.duoc.gamer.eventos.model.DestinoCompartido;
import java.util.Map;

public record ResumenCompartidosResponse(long total, Map<DestinoCompartido, Long> porDestino,
        boolean compartidoEnSitio) {
}
