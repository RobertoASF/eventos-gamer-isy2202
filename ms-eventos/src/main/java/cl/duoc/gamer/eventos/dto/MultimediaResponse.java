package cl.duoc.gamer.eventos.dto;

import cl.duoc.gamer.eventos.model.TipoMultimedia;
import cl.duoc.gamer.eventos.security.UsuarioToken;
import java.time.LocalDateTime;

public record MultimediaResponse(
        Long id,
        String autor,
        TipoMultimedia tipo,
        String tipoContenido,
        int tamano,
        String descripcion,
        LocalDateTime fechaCreacion,
        boolean puedeEliminar) {

    public static MultimediaResponse desde(MultimediaInfo info, UsuarioToken usuario) {
        return new MultimediaResponse(info.id(), info.autor(), info.tipo(), info.tipoContenido(), info.tamano(),
                info.descripcion(), info.fechaCreacion(), usuario.puedeEliminar(info.usuarioId()));
    }
}
