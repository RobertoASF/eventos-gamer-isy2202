package cl.duoc.gamer.usuarios.service;

import cl.duoc.gamer.usuarios.dto.UsuarioResponse;
import cl.duoc.gamer.usuarios.exception.RecursoNoEncontradoException;
import cl.duoc.gamer.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public UsuarioResponse perfil(String nombreUsuario) {
        return usuarioRepository.findByNombreUsuario(nombreUsuario)
                .map(UsuarioResponse::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario ya no existe"));
    }
}
