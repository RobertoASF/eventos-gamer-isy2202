package cl.duoc.gamer.eventos.repository;

import cl.duoc.gamer.eventos.model.Videojuego;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideojuegoRepository extends JpaRepository<Videojuego, Long> {

    List<Videojuego> findAllByOrderByNombreAsc();
}
