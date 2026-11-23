package cl.duoc.gamer.eventos.service;

import cl.duoc.gamer.eventos.dto.ContenidoMultimedia;
import cl.duoc.gamer.eventos.dto.MultimediaInfo;
import cl.duoc.gamer.eventos.dto.MultimediaResponse;
import cl.duoc.gamer.eventos.exception.AccionNoPermitidaException;
import cl.duoc.gamer.eventos.exception.RecursoNoEncontradoException;
import cl.duoc.gamer.eventos.exception.SolicitudInvalidaException;
import cl.duoc.gamer.eventos.model.Multimedia;
import cl.duoc.gamer.eventos.repository.EventoRepository;
import cl.duoc.gamer.eventos.repository.MultimediaRepository;
import cl.duoc.gamer.eventos.security.UsuarioToken;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class MultimediaService {

    // Limite por usuario y evento, para que nadie llene la base de datos subiendo archivos sin parar
    static final int MAXIMO_POR_USUARIO = 10;

    private static final String NO_EXISTE = "No existe la foto o el video solicitado";

    private final MultimediaRepository multimediaRepository;
    private final EventoRepository eventoRepository;
    private final EventoService eventoService;
    private final ValidadorMultimedia validador;

    @Transactional(readOnly = true)
    public List<MultimediaResponse> listar(Long eventoId, UsuarioToken usuario) {
        eventoService.verificarQueExiste(eventoId);
        return multimediaRepository.listarPorEvento(eventoId).stream()
                .map(info -> MultimediaResponse.desde(info, usuario))
                .toList();
    }

    @Transactional
    public MultimediaResponse agregar(Long eventoId, UsuarioToken usuario, MultipartFile archivo, String descripcion) {
        eventoService.verificarQueExiste(eventoId);
        if (multimediaRepository.countByEventoIdAndUsuarioId(eventoId, usuario.id()) >= MAXIMO_POR_USUARIO) {
            throw new SolicitudInvalidaException("Puedes subir hasta " + MAXIMO_POR_USUARIO
                    + " fotos o videos por evento");
        }
        ArchivoValidado validado = validador.validar(leer(archivo));

        Multimedia multimedia = new Multimedia();
        multimedia.setEvento(eventoRepository.getReferenceById(eventoId));
        multimedia.setUsuarioId(usuario.id());
        multimedia.setUsuario(usuario.nombreUsuario());
        multimedia.setAutor(usuario.nombre());
        multimedia.setTipo(validado.tipo());
        multimedia.setTipoContenido(validado.tipoContenido());
        multimedia.setTamano(validado.contenido().length);
        multimedia.setDescripcion(TextoSeguro.linea(descripcion));
        multimedia.setContenido(validado.contenido());
        multimedia.setFechaCreacion(LocalDateTime.now());
        Multimedia guardada = multimediaRepository.save(multimedia);

        log.info("Usuario {} agrego {} ({} bytes) al evento {}", usuario.nombreUsuario(), validado.tipo(),
                validado.contenido().length, eventoId);
        MultimediaInfo info = new MultimediaInfo(guardada.getId(), guardada.getUsuarioId(), guardada.getAutor(),
                guardada.getTipo(), guardada.getTipoContenido(), guardada.getTamano(), guardada.getDescripcion(),
                guardada.getFechaCreacion());
        return MultimediaResponse.desde(info, usuario);
    }

    @Transactional(readOnly = true)
    public ContenidoMultimedia contenido(Long eventoId, Long id) {
        return multimediaRepository.buscarContenido(eventoId, id)
                .orElseThrow(() -> new RecursoNoEncontradoException(NO_EXISTE));
    }

    @Transactional
    public void eliminar(Long eventoId, Long id, UsuarioToken usuario) {
        MultimediaInfo info = multimediaRepository.buscarInfo(eventoId, id)
                .orElseThrow(() -> new RecursoNoEncontradoException(NO_EXISTE));
        if (!usuario.puedeEliminar(info.usuarioId())) {
            log.warn("Usuario {} intento eliminar la multimedia {} de otro usuario", usuario.nombreUsuario(), id);
            throw new AccionNoPermitidaException("Solo puedes eliminar las fotos y videos que subiste");
        }
        multimediaRepository.eliminar(id);
        log.info("Usuario {} elimino la multimedia {} del evento {}", usuario.nombreUsuario(), id, eventoId);
    }

    private byte[] leer(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new SolicitudInvalidaException("Debes adjuntar una foto o un video");
        }
        try {
            return archivo.getBytes();
        } catch (IOException ex) {
            throw new SolicitudInvalidaException("No se pudo leer el archivo enviado");
        }
    }
}
