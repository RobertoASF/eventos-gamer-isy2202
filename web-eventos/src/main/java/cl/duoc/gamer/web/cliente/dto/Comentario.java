package cl.duoc.gamer.web.cliente.dto;

import java.time.LocalDateTime;

public record Comentario(
        Long id,
        String autor,
        int puntuacion,
        String texto,
        LocalDateTime fecha,
        boolean editado,
        boolean propio,
        boolean puedeEliminar) {
}
