package cl.duoc.gamer.eventos.service;

import cl.duoc.gamer.eventos.dto.CompartidoResponse;
import cl.duoc.gamer.eventos.dto.CompartirRequest;
import cl.duoc.gamer.eventos.dto.ResumenCompartidosResponse;
import cl.duoc.gamer.eventos.model.Compartido;
import cl.duoc.gamer.eventos.model.DestinoCompartido;
import cl.duoc.gamer.eventos.repository.CompartidoRepository;
import cl.duoc.gamer.eventos.repository.EventoRepository;
import cl.duoc.gamer.eventos.security.UsuarioToken;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompartidoService {

    private final CompartidoRepository compartidoRepository;
    private final EventoRepository eventoRepository;
    private final EventoService eventoService;

    // Se guarda una vez por usuario, evento y destino; si vuelve a compartir se actualiza la fecha y el mensaje
    @Transactional
    public ResumenCompartidosResponse compartir(Long eventoId, UsuarioToken usuario, CompartirRequest datos) {
        eventoService.verificarQueExiste(eventoId);
        DestinoCompartido destino = datos.destino();
        Compartido compartido = compartidoRepository
                .findByEventoIdAndUsuarioIdAndDestino(eventoId, usuario.id(), destino)
                .orElseGet(() -> nuevo(eventoId, usuario, destino));
        String mensaje = destino == DestinoCompartido.SITIO ? TextoSeguro.parrafos(datos.mensaje()) : "";
        compartido.setMensaje(mensaje.isEmpty() ? null : mensaje);
        compartido.setFechaCreacion(LocalDateTime.now());
        compartidoRepository.save(compartido);
        log.info("Usuario {} compartio el evento {} en {}", usuario.nombreUsuario(), eventoId, destino);
        return resumen(eventoId, usuario);
    }

    @Transactional(readOnly = true)
    public ResumenCompartidosResponse resumen(Long eventoId, UsuarioToken usuario) {
        eventoService.verificarQueExiste(eventoId);
        Map<DestinoCompartido, Long> porDestino = new EnumMap<>(DestinoCompartido.class);
        for (Object[] fila : compartidoRepository.contarPorDestino(eventoId)) {
            porDestino.put((DestinoCompartido) fila[0], (Long) fila[1]);
        }
        long total = porDestino.values().stream().mapToLong(Long::longValue).sum();
        boolean enSitio = compartidoRepository.existsByEventoIdAndUsuarioIdAndDestino(eventoId, usuario.id(),
                DestinoCompartido.SITIO);
        return new ResumenCompartidosResponse(total, porDestino, enSitio);
    }

    // Lo que la comunidad compartio en el sitio, del mas nuevo al mas antiguo
    @Transactional(readOnly = true)
    public List<CompartidoResponse> comunidad(int limite) {
        return compartidoRepository.findByDestinoOrderByFechaCreacionDesc(DestinoCompartido.SITIO,
                        PageRequest.of(0, limite)).stream()
                .map(CompartidoResponse::desde)
                .toList();
    }

    private Compartido nuevo(Long eventoId, UsuarioToken usuario, DestinoCompartido destino) {
        Compartido compartido = new Compartido();
        compartido.setEvento(eventoRepository.getReferenceById(eventoId));
        compartido.setUsuarioId(usuario.id());
        compartido.setUsuario(usuario.nombreUsuario());
        compartido.setAutor(usuario.nombre());
        compartido.setDestino(destino);
        return compartido;
    }
}
