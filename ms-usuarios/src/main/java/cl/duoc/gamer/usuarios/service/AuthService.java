package cl.duoc.gamer.usuarios.service;

import cl.duoc.gamer.usuarios.dto.LoginRequest;
import cl.duoc.gamer.usuarios.dto.LoginResponse;
import cl.duoc.gamer.usuarios.dto.UsuarioResponse;
import cl.duoc.gamer.usuarios.exception.CredencialesInvalidasException;
import cl.duoc.gamer.usuarios.exception.CuentaBloqueadaException;
import cl.duoc.gamer.usuarios.model.Usuario;
import cl.duoc.gamer.usuarios.repository.UsuarioRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final IntentosLoginService intentosLogin;

    // Hash de una clave al azar. Se compara contra el cuando el usuario no existe,
    // asi la respuesta tarda lo mismo y no revela que nombres de usuario estan registrados
    private final String hashSinUsuario;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
            TokenService tokenService, IntentosLoginService intentosLogin) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.intentosLogin = intentosLogin;
        this.hashSinUsuario = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest datos) {
        String nombreUsuario = datos.nombreUsuario().trim();
        if (intentosLogin.estaBloqueado(nombreUsuario)) {
            log.warn("Login rechazado: usuario {} bloqueado por intentos fallidos", paraLog(nombreUsuario));
            throw new CuentaBloqueadaException();
        }

        Optional<Usuario> encontrado = usuarioRepository.findByNombreUsuario(nombreUsuario);
        String hash = encontrado.map(Usuario::getPasswordHash).orElse(hashSinUsuario);
        boolean claveCorrecta = passwordEncoder.matches(datos.password(), hash);

        if (encontrado.isEmpty() || !claveCorrecta || !encontrado.get().isActivo()) {
            intentosLogin.registrarFallido(nombreUsuario);
            log.warn("Login fallido para el usuario {}", paraLog(nombreUsuario));
            throw new CredencialesInvalidasException();
        }

        Usuario usuario = encontrado.get();
        intentosLogin.registrarExitoso(nombreUsuario);
        log.info("Login correcto del usuario {} (id {})", usuario.getNombreUsuario(), usuario.getId());
        return new LoginResponse(tokenService.generar(usuario), "Bearer", tokenService.segundosValidez(),
                UsuarioResponse.desde(usuario));
    }

    // El nombre viene del usuario: se reemplazan saltos de linea y cualquier otro caracter de control
    // para que no pueda inventar lineas en el log ni ensuciarlo con bytes nulos
    private String paraLog(String texto) {
        String limpio = texto.replaceAll("\\p{Cntrl}", "_");
        return limpio.length() > 30 ? limpio.substring(0, 30) + "..." : limpio;
    }
}
