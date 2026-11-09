package cl.duoc.gamer.usuarios.controller;

import cl.duoc.gamer.usuarios.dto.UsuarioResponse;
import cl.duoc.gamer.usuarios.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class PerfilController {

    private final UsuarioService usuarioService;

    // El usuario se toma del token, nunca de un parametro de la peticion
    @GetMapping("/perfil")
    public UsuarioResponse perfil(@AuthenticationPrincipal Jwt jwt) {
        return usuarioService.perfil(jwt.getSubject());
    }
}
