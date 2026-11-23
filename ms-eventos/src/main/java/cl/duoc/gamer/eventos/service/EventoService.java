package cl.duoc.gamer.eventos.service;

import cl.duoc.gamer.eventos.dto.EventoDetalleResponse;
import cl.duoc.gamer.eventos.dto.EventoResumenResponse;
import cl.duoc.gamer.eventos.dto.FiltroEventosRequest;
import cl.duoc.gamer.eventos.dto.FiltrosResponse;
import cl.duoc.gamer.eventos.dto.OpcionResponse;
import cl.duoc.gamer.eventos.dto.PaginaResponse;
import cl.duoc.gamer.eventos.exception.RecursoNoEncontradoException;
import cl.duoc.gamer.eventos.model.Evento;
import cl.duoc.gamer.eventos.model.Modalidad;
import cl.duoc.gamer.eventos.repository.CategoriaRepository;
import cl.duoc.gamer.eventos.repository.EventoRepository;
import cl.duoc.gamer.eventos.repository.VideojuegoRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final VideojuegoRepository videojuegoRepository;
    private final CategoriaRepository categoriaRepository;

    // Eventos que aun no ocurren, del publicado mas nuevo al mas antiguo
    @Transactional(readOnly = true)
    public List<EventoResumenResponse> recientes(int limite) {
        PageRequest orden = PageRequest.of(0, limite, Sort.by(Sort.Direction.DESC, "fechaPublicacion"));
        return eventoRepository.findByFechaInicioAfter(LocalDateTime.now(), orden).stream()
                .map(EventoResumenResponse::desde)
                .toList();
    }

    // Eventos que aun no ocurren, del que tiene mas interesados al que tiene menos
    @Transactional(readOnly = true)
    public List<EventoResumenResponse> populares(int limite) {
        PageRequest orden = PageRequest.of(0, limite, Sort.by(Sort.Direction.DESC, "interesados"));
        return eventoRepository.findByFechaInicioAfter(LocalDateTime.now(), orden).stream()
                .map(EventoResumenResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaginaResponse<EventoResumenResponse> buscar(FiltroEventosRequest filtro, int pagina, int tamano) {
        PageRequest orden = PageRequest.of(pagina, tamano, Sort.by(Sort.Direction.ASC, "fechaInicio", "id"));
        Page<Evento> resultado = eventoRepository.findAll(filtro(filtro), orden);
        List<EventoResumenResponse> eventos = resultado.getContent().stream()
                .map(EventoResumenResponse::desde)
                .toList();
        return PaginaResponse.de(resultado, eventos);
    }

    @Transactional(readOnly = true)
    public EventoDetalleResponse detalle(Long id) {
        return eventoRepository.findDetalleById(id)
                .map(EventoDetalleResponse::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el evento con id " + id));
    }

    // Las fotos, comentarios y compartidos siempre pertenecen a un evento publicado
    @Transactional(readOnly = true)
    public void verificarQueExiste(Long id) {
        if (!eventoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe el evento con id " + id);
        }
    }

    @Transactional(readOnly = true)
    public FiltrosResponse filtros() {
        List<OpcionResponse> videojuegos = videojuegoRepository.findAllByOrderByNombreAsc().stream()
                .map(videojuego -> new OpcionResponse(videojuego.getId(), videojuego.getNombre()))
                .toList();
        List<OpcionResponse> categorias = categoriaRepository.findAllByOrderByNombreAsc().stream()
                .map(categoria -> new OpcionResponse(categoria.getId(), categoria.getNombre()))
                .toList();
        return new FiltrosResponse(videojuegos, categorias, eventoRepository.listarPaises(),
                eventoRepository.listarCiudades(), Arrays.asList(Modalidad.values()));
    }

    // Arma la consulta solo con los filtros que vienen. Los valores van como parametros, nunca concatenados al SQL
    private Specification<Evento> filtro(FiltroEventosRequest filtro) {
        return (raiz, consulta, criterio) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (tieneTexto(filtro.nombre())) {
                String patron = "%" + escaparComodines(filtro.nombre().trim().toLowerCase(Locale.ROOT)) + "%";
                condiciones.add(criterio.like(criterio.lower(raiz.get("nombre")), patron, '\\'));
            }
            if (filtro.videojuegoId() != null) {
                condiciones.add(criterio.equal(raiz.get("videojuego").get("id"), filtro.videojuegoId()));
            }
            if (filtro.categoriaId() != null) {
                condiciones.add(criterio.equal(raiz.get("categoria").get("id"), filtro.categoriaId()));
            }
            if (tieneTexto(filtro.pais())) {
                condiciones.add(criterio.equal(raiz.get("pais"), filtro.pais().trim()));
            }
            if (tieneTexto(filtro.ciudad())) {
                condiciones.add(criterio.equal(raiz.get("ciudad"), filtro.ciudad().trim()));
            }
            if (filtro.modalidad() != null) {
                condiciones.add(criterio.equal(raiz.get("modalidad"), filtro.modalidad()));
            }
            if (filtro.desde() != null) {
                condiciones.add(criterio.greaterThanOrEqualTo(raiz.get("fechaInicio"), filtro.desde().atStartOfDay()));
            }
            if (filtro.hasta() != null) {
                condiciones.add(criterio.lessThan(raiz.get("fechaInicio"), filtro.hasta().plusDays(1).atStartOfDay()));
            }
            return criterio.and(condiciones.toArray(new Predicate[0]));
        };
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

    // Evita que un % o _ escrito por el usuario funcione como comodin del LIKE
    private String escaparComodines(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
