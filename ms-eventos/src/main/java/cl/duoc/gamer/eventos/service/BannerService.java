package cl.duoc.gamer.eventos.service;

import cl.duoc.gamer.eventos.dto.BannerResponse;
import cl.duoc.gamer.eventos.repository.BannerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;

    @Transactional(readOnly = true)
    public List<BannerResponse> activos() {
        return bannerRepository.findByActivoTrueOrderByOrdenAsc().stream()
                .map(BannerResponse::desde)
                .toList();
    }
}
