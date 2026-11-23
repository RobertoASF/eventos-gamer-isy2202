package cl.duoc.gamer.eventos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.gamer.eventos.dto.ComentarioRequest;
import cl.duoc.gamer.eventos.exception.AccionNoPermitidaException;
import cl.duoc.gamer.eventos.exception.SolicitudInvalidaException;
import cl.duoc.gamer.eventos.model.Comentario;
import cl.duoc.gamer.eventos.repository.ComentarioRepository;
import cl.duoc.gamer.eventos.repository.EventoRepository;
import cl.duoc.gamer.eventos.security.UsuarioToken;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ComentarioServiceTest {

    private static final UsuarioToken VALE = new UsuarioToken(2L, "vale", "Valentina Mora", false);
    private static final UsuarioToken MATI = new UsuarioToken(3L, "mati", "Matías Fuentes", false);
    private static final UsuarioToken ADMIN = new UsuarioToken(1L, "admin", "Administrador", true);

    private ComentarioRepository comentarioRepository;
    private ComentarioService servicio;

    @BeforeEach
    void preparar() {
        comentarioRepository = mock(ComentarioRepository.class);
        servicio = new ComentarioService(comentarioRepository, mock(EventoRepository.class), mock(EventoService.class));
        when(comentarioRepository.save(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    @Test
    void elAutorDelComentarioLoTomaDelTokenYNoDeLaPeticion() {
        when(comentarioRepository.findByEventoIdAndUsuarioId(1L, 2L)).thenReturn(Optional.empty());

        ComentarioService.Resultado resultado = servicio.guardar(1L, VALE, new ComentarioRequest(5, "Muy bueno"));

        assertThat(resultado.creado()).isTrue();
        assertThat(resultado.comentario().autor()).isEqualTo("Valentina Mora");
        assertThat(resultado.comentario().propio()).isTrue();
    }

    @Test
    void siYaOpinoActualizaSuOpinionEnVezDeCrearOtra() {
        Comentario existente = comentario(2L);
        when(comentarioRepository.findByEventoIdAndUsuarioId(1L, 2L)).thenReturn(Optional.of(existente));

        ComentarioService.Resultado resultado = servicio.guardar(1L, VALE, new ComentarioRequest(3, "Cambié de opinión"));

        assertThat(resultado.creado()).isFalse();
        assertThat(resultado.comentario().puntuacion()).isEqualTo(3);
        assertThat(resultado.comentario().editado()).isTrue();
    }

    @Test
    void quitaCaracteresDeControlDelTexto() {
        when(comentarioRepository.findByEventoIdAndUsuarioId(1L, 2L)).thenReturn(Optional.empty());

        ComentarioService.Resultado resultado = servicio.guardar(1L, VALE,
                new ComentarioRequest(4, "Linea uno\u0000\u001b[31m\r\nLinea dos"));

        assertThat(resultado.comentario().texto()).isEqualTo("Linea uno  [31m\nLinea dos");
    }

    @Test
    void rechazaUnComentarioQueSoloTieneCaracteresDeControl() {
        ComentarioRequest soloControl = new ComentarioRequest(4, "\u0000\u0007");

        assertThatThrownBy(() -> servicio.guardar(1L, VALE, soloControl))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void unUsuarioNoPuedeEliminarElComentarioDeOtro() {
        Comentario deVale = comentario(2L);
        when(comentarioRepository.findByIdAndEventoId(10L, 1L)).thenReturn(Optional.of(deVale));

        assertThatThrownBy(() -> servicio.eliminar(1L, 10L, MATI)).isInstanceOf(AccionNoPermitidaException.class);
        verify(comentarioRepository, never()).delete(any());
    }

    @Test
    void elAutorYElAdministradorSiPuedenEliminar() {
        Comentario deVale = comentario(2L);
        when(comentarioRepository.findByIdAndEventoId(10L, 1L)).thenReturn(Optional.of(deVale));

        servicio.eliminar(1L, 10L, VALE);
        servicio.eliminar(1L, 10L, ADMIN);

        verify(comentarioRepository, org.mockito.Mockito.times(2)).delete(deVale);
    }

    private Comentario comentario(Long usuarioId) {
        Comentario comentario = new Comentario();
        comentario.setId(10L);
        comentario.setUsuarioId(usuarioId);
        comentario.setAutor("Valentina Mora");
        comentario.setPuntuacion(5);
        comentario.setTexto("Texto");
        comentario.setFechaCreacion(LocalDateTime.of(2026, 11, 20, 18, 0));
        return comentario;
    }
}
