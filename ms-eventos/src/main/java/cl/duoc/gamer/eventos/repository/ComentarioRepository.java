package cl.duoc.gamer.eventos.repository;

import cl.duoc.gamer.eventos.model.Comentario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByEventoIdOrderByFechaCreacionDesc(Long eventoId);

    Optional<Comentario> findByEventoIdAndUsuarioId(Long eventoId, Long usuarioId);

    Optional<Comentario> findByIdAndEventoId(Long id, Long eventoId);
}
