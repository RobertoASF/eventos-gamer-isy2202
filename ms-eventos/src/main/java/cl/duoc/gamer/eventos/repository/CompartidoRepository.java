package cl.duoc.gamer.eventos.repository;

import cl.duoc.gamer.eventos.model.Compartido;
import cl.duoc.gamer.eventos.model.DestinoCompartido;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompartidoRepository extends JpaRepository<Compartido, Long> {

    Optional<Compartido> findByEventoIdAndUsuarioIdAndDestino(Long eventoId, Long usuarioId,
            DestinoCompartido destino);

    boolean existsByEventoIdAndUsuarioIdAndDestino(Long eventoId, Long usuarioId, DestinoCompartido destino);

    @Query("select c.destino, count(c) from Compartido c where c.evento.id = :eventoId group by c.destino")
    List<Object[]> contarPorDestino(@Param("eventoId") Long eventoId);

    @EntityGraph(attributePaths = {"evento", "evento.videojuego", "evento.categoria"})
    List<Compartido> findByDestinoOrderByFechaCreacionDesc(DestinoCompartido destino, Pageable pagina);
}
