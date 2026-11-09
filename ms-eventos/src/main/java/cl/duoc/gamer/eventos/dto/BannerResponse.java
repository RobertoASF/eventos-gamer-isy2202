package cl.duoc.gamer.eventos.dto;

import cl.duoc.gamer.eventos.model.Banner;

public record BannerResponse(Long id, String marca, String titulo, String mensaje, String imagen) {

    public static BannerResponse desde(Banner banner) {
        return new BannerResponse(banner.getId(), banner.getMarca(), banner.getTitulo(), banner.getMensaje(),
                banner.getImagen());
    }
}
