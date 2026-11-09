package cl.duoc.gamer.web.cliente.dto;

import java.util.List;

public record Filtros(
        List<Opcion> videojuegos,
        List<Opcion> categorias,
        List<String> paises,
        List<String> ciudades,
        List<String> modalidades) {
}
