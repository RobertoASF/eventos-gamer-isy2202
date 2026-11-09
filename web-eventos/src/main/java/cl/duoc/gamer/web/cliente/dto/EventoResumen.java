package cl.duoc.gamer.web.cliente.dto;

import java.time.LocalDateTime;

public record EventoResumen(
        Long id,
        String nombre,
        String resumen,
        String videojuego,
        String categoria,
        String modalidad,
        String pais,
        String ciudad,
        LocalDateTime fechaInicio,
        int valorEntrada,
        int interesados,
        String imagenPortada) {
}
