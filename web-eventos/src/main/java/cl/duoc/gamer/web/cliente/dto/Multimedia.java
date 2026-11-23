package cl.duoc.gamer.web.cliente.dto;

import java.time.LocalDateTime;

public record Multimedia(
        Long id,
        String autor,
        String tipo,
        String tipoContenido,
        int tamano,
        String descripcion,
        LocalDateTime fechaCreacion,
        boolean puedeEliminar) {

    public boolean esVideo() {
        return "VIDEO".equals(tipo);
    }
}
