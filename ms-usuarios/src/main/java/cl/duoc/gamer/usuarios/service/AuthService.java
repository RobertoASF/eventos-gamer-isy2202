package cl.duoc.gamer.usuarios.service;

import cl.duoc.gamer.usuarios.dto.LoginRequest;
import cl.duoc.gamer.usuarios.dto.LoginResponse;
import cl.duoc.gamer.usuarios.dto.UsuarioResponse;
import cl.duoc.gamer.usuarios.exception.CredencialesInvalidasException;
import cl.duoc.gamer.usuarios.model.Usuario;
import cl.duoc.gamer.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest datos) {
        Usuario usuario = usuarioRepository.findByNombreUsuario(datos.nombreUsuario())
                .filter(Usuario::isActivo)
                .filter(encontrado -> passwordEncoder.matches(datos.password(), encontrado.getPasswordHash()))
                .orElseThrow(CredencialesInvalidasException::new);

        return new LoginResponse(tokenService.generar(usuario), "Bearer", tokenService.segundosValidez(),
                UsuarioResponse.desde(usuario));
    }
}
