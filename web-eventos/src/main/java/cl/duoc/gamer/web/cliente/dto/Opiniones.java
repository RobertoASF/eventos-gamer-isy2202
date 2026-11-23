package cl.duoc.gamer.web.cliente.dto;

import java.util.List;

public record Opiniones(double promedio, long total, List<Comentario> comentarios, Comentario miOpinion) {

    // Cantidad de estrellas llenas que se dibujan para el promedio (redondeado al entero mas cercano)
    public int estrellasPromedio() {
        return (int) Math.round(promedio);
    }
}
