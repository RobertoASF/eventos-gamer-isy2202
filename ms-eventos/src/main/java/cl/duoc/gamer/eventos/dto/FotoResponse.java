package cl.duoc.gamer.eventos.dto;

import cl.duoc.gamer.eventos.model.FotoEvento;

public record FotoResponse(String url, String descripcion) {

    public static FotoResponse desde(FotoEvento foto) {
        return new FotoResponse(foto.getUrl(), foto.getDescripcion());
    }
}
