package cl.duoc.gamer.eventos.dto;

import cl.duoc.gamer.eventos.model.Evento;
import cl.duoc.gamer.eventos.model.Modalidad;
import java.time.LocalDateTime;

public record EventoResumenResponse(
        Long id,
        String nombre,
        String resumen,
        String videojuego,
        String categoria,
        Modalidad modalidad,
        String pais,
        String ciudad,
        LocalDateTime fechaInicio,
        int valorEntrada,
        int interesados,
        String imagenPortada) {

    public static EventoResumenResponse desde(Evento evento) {
        return new EventoResumenResponse(
                evento.getId(),
                evento.getNombre(),
                evento.getResumen(),
                evento.getVideojuego().getNombre(),
                evento.getCategoria().getNombre(),
                evento.getModalidad(),
                evento.getPais(),
                evento.getCiudad(),
                evento.getFechaInicio(),
                evento.getValorEntrada(),
                evento.getInteresados(),
                evento.getImagenPortada());
    }
}
