package cl.duoc.gamer.eventos.controller;

import cl.duoc.gamer.eventos.dto.BannerResponse;
import cl.duoc.gamer.eventos.service.BannerService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping
    public List<BannerResponse> activos() {
        return bannerService.activos();
    }
}
