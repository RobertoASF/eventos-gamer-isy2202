package cl.duoc.gamer.eventos.repository;

import cl.duoc.gamer.eventos.model.Banner;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BannerRepository extends JpaRepository<Banner, Long> {

    List<Banner> findByActivoTrueOrderByOrdenAsc();
}
