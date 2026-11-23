package cl.duoc.gamer.web.cliente.dto;

import java.time.LocalDateTime;

public record Compartido(Long id, String autor, String destino, String mensaje, LocalDateTime fecha,
        EventoResumen evento) {
}
