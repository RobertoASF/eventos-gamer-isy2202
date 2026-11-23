package cl.duoc.gamer.eventos.dto;

import cl.duoc.gamer.eventos.model.TipoMultimedia;
import java.time.LocalDateTime;

// Datos de una foto o video sin el archivo. Lo arma la consulta del repositorio
public record MultimediaInfo(
        Long id,
        Long usuarioId,
        String autor,
        TipoMultimedia tipo,
        String tipoContenido,
        int tamano,
        String descripcion,
        LocalDateTime fechaCreacion) {
}
