package cl.duoc.gamer.eventos.repository;

import cl.duoc.gamer.eventos.model.Evento;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface EventoRepository extends JpaRepository<Evento, Long>, JpaSpecificationExecutor<Evento> {

    @Override
    @EntityGraph(attributePaths = {"videojuego", "categoria"})
    Page<Evento> findAll(Specification<Evento> filtro, Pageable pagina);

    @EntityGraph(attributePaths = {"videojuego", "categoria"})
    List<Evento> findByFechaInicioAfter(LocalDateTime fecha, Pageable pagina);

    @EntityGraph(attributePaths = {"videojuego", "categoria", "fotos"})
    Optional<Evento> findDetalleById(Long id);

    @Query("select distinct e.pais from Evento e order by e.pais")
    List<String> listarPaises();

    @Query("select distinct e.ciudad from Evento e order by e.ciudad")
    List<String> listarCiudades();
}
