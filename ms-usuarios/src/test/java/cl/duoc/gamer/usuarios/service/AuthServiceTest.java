package cl.duoc.gamer.usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.gamer.usuarios.dto.LoginRequest;
import cl.duoc.gamer.usuarios.dto.LoginResponse;
import cl.duoc.gamer.usuarios.exception.CredencialesInvalidasException;
import cl.duoc.gamer.usuarios.exception.CuentaBloqueadaException;
import cl.duoc.gamer.usuarios.model.Rol;
import cl.duoc.gamer.usuarios.model.Usuario;
import cl.duoc.gamer.usuarios.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

    private static final String CLAVE = "Gamer2026*";

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private UsuarioRepository usuarioRepository;
    private TokenService tokenService;
    private IntentosLoginService intentos;
    private AuthService authService;

    @BeforeEach
    void preparar() {
        usuarioRepository = mock(UsuarioRepository.class);
        tokenService = mock(TokenService.class);
        intentos = mock(IntentosLoginService.class);
        when(tokenService.generar(any())).thenReturn("token-de-prueba");
        when(tokenService.segundosValidez()).thenReturn(3600L);
        authService = new AuthService(usuarioRepository, passwordEncoder, tokenService, intentos);
    }

    @Test
    void entregaTokenConCredencialesCorrectas() {
        when(usuarioRepository.findByNombreUsuario("mati")).thenReturn(Optional.of(usuario(true)));

        LoginResponse respuesta = authService.login(new LoginRequest("mati", CLAVE));

        assertThat(respuesta.token()).isEqualTo("token-de-prueba");
        assertThat(respuesta.usuario().nombreUsuario()).isEqualTo("mati");
        verify(intentos).registrarExitoso("mati");
    }

    @Test
    void rechazaClaveIncorrectaYCuentaElIntento() {
        when(usuarioRepository.findByNombreUsuario("mati")).thenReturn(Optional.of(usuario(true)));

        assertThatThrownBy(() -> authService.login(new LoginRequest("mati", "otra-clave")))
                .isInstanceOf(CredencialesInvalidasException.class);
        verify(intentos).registrarFallido("mati");
    }

    @Test
    void usuarioInexistenteRecibeElMismoErrorQueUnaClaveIncorrecta() {
        when(usuarioRepository.findByNombreUsuario(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("no-existe", CLAVE)))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Usuario o clave incorrectos");
    }

    @Test
    void rechazaUsuarioInactivoAunqueLaClaveSeaCorrecta() {
        when(usuarioRepository.findByNombreUsuario("mati")).thenReturn(Optional.of(usuario(false)));

        assertThatThrownBy(() -> authService.login(new LoginRequest("mati", CLAVE)))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void noRevisaLaClaveSiLaCuentaEstaBloqueada() {
        when(intentos.estaBloqueado("mati")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginRequest("mati", CLAVE)))
                .isInstanceOf(CuentaBloqueadaException.class);
        verify(usuarioRepository, never()).findByNombreUsuario(anyString());
    }

    private Usuario usuario(boolean activo) {
        Rol rol = new Rol();
        rol.setId(2);
        rol.setNombre("USUARIO");
        Usuario usuario = new Usuario();
        usuario.setId(3L);
        usuario.setNombreUsuario("mati");
        usuario.setNombre("Matías Fuentes");
        usuario.setEmail("mati@eventosgamer.cl");
        usuario.setPasswordHash(passwordEncoder.encode(CLAVE));
        usuario.setRol(rol);
        usuario.setActivo(activo);
        return usuario;
    }
}
