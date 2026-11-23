package cl.duoc.gamer.eventos.repository;

import cl.duoc.gamer.eventos.dto.ContenidoMultimedia;
import cl.duoc.gamer.eventos.dto.MultimediaInfo;
import cl.duoc.gamer.eventos.model.Multimedia;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MultimediaRepository extends JpaRepository<Multimedia, Long> {

    @Query("""
            select new cl.duoc.gamer.eventos.dto.MultimediaInfo(m.id, m.usuarioId, m.autor, m.tipo, m.tipoContenido,
                m.tamano, m.descripcion, m.fechaCreacion)
            from Multimedia m where m.evento.id = :eventoId order by m.fechaCreacion desc, m.id desc""")
    List<MultimediaInfo> listarPorEvento(@Param("eventoId") Long eventoId);

    @Query("""
            select new cl.duoc.gamer.eventos.dto.MultimediaInfo(m.id, m.usuarioId, m.autor, m.tipo, m.tipoContenido,
                m.tamano, m.descripcion, m.fechaCreacion)
            from Multimedia m where m.id = :id and m.evento.id = :eventoId""")
    Optional<MultimediaInfo> buscarInfo(@Param("eventoId") Long eventoId, @Param("id") Long id);

    @Query("""
            select new cl.duoc.gamer.eventos.dto.ContenidoMultimedia(m.tipoContenido, m.contenido)
            from Multimedia m where m.id = :id and m.evento.id = :eventoId""")
    Optional<ContenidoMultimedia> buscarContenido(@Param("eventoId") Long eventoId, @Param("id") Long id);

    long countByEventoIdAndUsuarioId(Long eventoId, Long usuarioId);

    @Modifying
    @Query("delete from Multimedia m where m.id = :id")
    void eliminar(@Param("id") Long id);
}
