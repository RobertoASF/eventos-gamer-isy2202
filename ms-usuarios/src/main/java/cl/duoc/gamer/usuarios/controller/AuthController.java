package cl.duoc.gamer.usuarios.controller;

import cl.duoc.gamer.usuarios.dto.LoginRequest;
import cl.duoc.gamer.usuarios.dto.LoginResponse;
import cl.duoc.gamer.usuarios.service.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Primer paso del login: entrega el token CSRF y lo deja tambien en la cookie XSRF-TOKEN.
    // El cliente debe devolver ambos en el POST /login
    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("cabecera", token.getHeaderName(), "token", token.getToken());
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest datos) {
        return authService.login(datos);
    }
}
