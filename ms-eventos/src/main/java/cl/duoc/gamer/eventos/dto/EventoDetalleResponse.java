package cl.duoc.gamer.eventos.dto;

import cl.duoc.gamer.eventos.model.Evento;
import cl.duoc.gamer.eventos.model.Modalidad;
import java.time.LocalDateTime;
import java.util.List;

public record EventoDetalleResponse(
        Long id,
        String nombre,
        String descripcion,
        String videojuego,
        String generoVideojuego,
        String categoria,
        Modalidad modalidad,
        String pais,
        String ciudad,
        String lugar,
        String enlaceAcceso,
        LocalDateTime fechaInicio,
        String organizador,
        String requisitos,
        String inscripcion,
        int valorEntrada,
        int cupos,
        int interesados,
        String imagenPortada,
        List<FotoResponse> fotos) {

    public static EventoDetalleResponse desde(Evento evento) {
        return new EventoDetalleResponse(
                evento.getId(),
                evento.getNombre(),
                evento.getDescripcion(),
                evento.getVideojuego().getNombre(),
                evento.getVideojuego().getGenero(),
                evento.getCategoria().getNombre(),
                evento.getModalidad(),
                evento.getPais(),
                evento.getCiudad(),
                evento.getLugar(),
                evento.getEnlaceAcceso(),
                evento.getFechaInicio(),
                evento.getOrganizador(),
                evento.getRequisitos(),
                evento.getInscripcion(),
                evento.getValorEntrada(),
                evento.getCupos(),
                evento.getInteresados(),
                evento.getImagenPortada(),
                evento.getFotos().stream().map(FotoResponse::desde).toList());
    }
}
