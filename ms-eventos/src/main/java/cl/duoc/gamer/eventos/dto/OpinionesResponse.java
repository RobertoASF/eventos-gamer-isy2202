package cl.duoc.gamer.eventos.dto;

import java.util.List;

// Valoracion promedio, cantidad de opiniones y la opinion del propio usuario (si ya opino)
public record OpinionesResponse(
        double promedio,
        long total,
        List<ComentarioResponse> comentarios,
        ComentarioResponse miOpinion) {
}
