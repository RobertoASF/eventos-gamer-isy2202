package cl.duoc.gamer.eventos.dto;

import cl.duoc.gamer.eventos.model.DestinoCompartido;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CompartirRequest(
        @NotNull(message = "Debes indicar dónde se comparte el evento")
        DestinoCompartido destino,

        @Size(max = 280, message = "El mensaje puede tener hasta 280 caracteres")
        String mensaje) {
}
