package cl.duoc.gamer.eventos.dto;

import cl.duoc.gamer.eventos.model.Modalidad;
import java.util.List;

// Opciones para llenar las listas del formulario de busqueda
public record FiltrosResponse(
        List<OpcionResponse> videojuegos,
        List<OpcionResponse> categorias,
        List<String> paises,
        List<String> ciudades,
        List<Modalidad> modalidades) {
}
