package cl.duoc.gamer.eventos.service;

import cl.duoc.gamer.eventos.dto.ComentarioRequest;
import cl.duoc.gamer.eventos.dto.ComentarioResponse;
import cl.duoc.gamer.eventos.dto.OpinionesResponse;
import cl.duoc.gamer.eventos.exception.AccionNoPermitidaException;
import cl.duoc.gamer.eventos.exception.RecursoNoEncontradoException;
import cl.duoc.gamer.eventos.exception.SolicitudInvalidaException;
import cl.duoc.gamer.eventos.model.Comentario;
import cl.duoc.gamer.eventos.repository.ComentarioRepository;
import cl.duoc.gamer.eventos.repository.EventoRepository;
import cl.duoc.gamer.eventos.security.UsuarioToken;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final EventoRepository eventoRepository;
    private final EventoService eventoService;

    @Transactional(readOnly = true)
    public OpinionesResponse listar(Long eventoId, UsuarioToken usuario) {
        eventoService.verificarQueExiste(eventoId);
        List<Comentario> comentarios = comentarioRepository.findByEventoIdOrderByFechaCreacionDesc(eventoId);
        List<ComentarioResponse> respuestas = comentarios.stream()
                .map(comentario -> ComentarioResponse.desde(comentario, usuario))
                .toList();
        double promedio = comentarios.stream().mapToInt(Comentario::getPuntuacion).average().orElse(0);
        ComentarioResponse miOpinion = respuestas.stream().filter(ComentarioResponse::propio).findFirst().orElse(null);
        return new OpinionesResponse(Math.round(promedio * 10) / 10.0, comentarios.size(), respuestas, miOpinion);
    }

    // Cada usuario tiene una opinion por evento: si ya opino, se actualiza la que tenia
    @Transactional
    public Resultado guardar(Long eventoId, UsuarioToken usuario, ComentarioRequest datos) {
        eventoService.verificarQueExiste(eventoId);
        String texto = TextoSeguro.parrafos(datos.texto());
        if (texto.isEmpty()) {
            throw new SolicitudInvalidaException("El comentario no puede quedar vacío");
        }
        Optional<Comentario> existente = comentarioRepository.findByEventoIdAndUsuarioId(eventoId, usuario.id());
        Comentario comentario = existente.orElseGet(() -> nuevo(eventoId, usuario));
        if (existente.isPresent()) {
            comentario.setFechaEdicion(LocalDateTime.now());
        }
        comentario.setPuntuacion(datos.puntuacion());
        comentario.setTexto(texto);
        Comentario guardado = comentarioRepository.save(comentario);
        log.info("Usuario {} {} su opinion del evento {} ({} estrellas)", usuario.nombreUsuario(),
                existente.isPresent() ? "actualizo" : "publico", eventoId, datos.puntuacion());
        return new Resultado(ComentarioResponse.desde(guardado, usuario), existente.isEmpty());
    }

    @Transactional
    public void eliminar(Long eventoId, Long id, UsuarioToken usuario) {
        Comentario comentario = comentarioRepository.findByIdAndEventoId(id, eventoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el comentario solicitado"));
        if (!usuario.puedeEliminar(comentario.getUsuarioId())) {
            log.warn("Usuario {} intento eliminar el comentario {} de otro usuario", usuario.nombreUsuario(), id);
            throw new AccionNoPermitidaException("Solo puedes eliminar tus propios comentarios");
        }
        comentarioRepository.delete(comentario);
        log.info("Usuario {} elimino el comentario {} del evento {}", usuario.nombreUsuario(), id, eventoId);
    }

    private Comentario nuevo(Long eventoId, UsuarioToken usuario) {
        Comentario comentario = new Comentario();
        comentario.setEvento(eventoRepository.getReferenceById(eventoId));
        comentario.setUsuarioId(usuario.id());
        comentario.setUsuario(usuario.nombreUsuario());
        comentario.setAutor(usuario.nombre());
        comentario.setFechaCreacion(LocalDateTime.now());
        return comentario;
    }

    public record Resultado(ComentarioResponse comentario, boolean creado) {
    }
}
