package cl.duoc.gamer.eventos.dto;

import cl.duoc.gamer.eventos.model.Comentario;
import cl.duoc.gamer.eventos.security.UsuarioToken;
import java.time.LocalDateTime;

public record ComentarioResponse(
        Long id,
        String autor,
        int puntuacion,
        String texto,
        LocalDateTime fecha,
        boolean editado,
        boolean propio,
        boolean puedeEliminar) {

    public static ComentarioResponse desde(Comentario comentario, UsuarioToken usuario) {
        boolean editado = comentario.getFechaEdicion() != null;
        return new ComentarioResponse(comentario.getId(), comentario.getAutor(), comentario.getPuntuacion(),
                comentario.getTexto(), editado ? comentario.getFechaEdicion() : comentario.getFechaCreacion(),
                editado, usuario.id().equals(comentario.getUsuarioId()),
                usuario.puedeEliminar(comentario.getUsuarioId()));
    }
}
