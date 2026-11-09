package cl.duoc.gamer.web.seguridad;

import cl.duoc.gamer.web.cliente.ClienteUsuarios;
import cl.duoc.gamer.web.cliente.dto.LoginRespuesta;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

// Valida el usuario y la clave contra la API de login de ms-usuarios en vez de una lista local
@Component
@RequiredArgsConstructor
public class ProveedorAutenticacionApi implements AuthenticationProvider {

    private final ClienteUsuarios clienteUsuarios;

    @Override
    public Authentication authenticate(Authentication authentication) {
        String nombreUsuario = authentication.getName();
        String password = String.valueOf(authentication.getCredentials());

        LoginRespuesta respuesta;
        try {
            respuesta = clienteUsuarios.login(nombreUsuario, password)
                    .orElseThrow(() -> new BadCredentialsException("Usuario o clave incorrectos"));
        } catch (ClienteUsuarios.CuentaBloqueadaException ex) {
            throw new LockedException("Cuenta bloqueada temporalmente", ex);
        } catch (RestClientException ex) {
            throw new AuthenticationServiceException("El servicio de usuarios no está disponible", ex);
        }

        UsuarioSesion usuario = new UsuarioSesion(respuesta.usuario().nombreUsuario(), respuesta.usuario().nombre(),
                respuesta.usuario().rol(), respuesta.token());
        List<SimpleGrantedAuthority> roles = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol()));
        return UsernamePasswordAuthenticationToken.authenticated(usuario, null, roles);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
