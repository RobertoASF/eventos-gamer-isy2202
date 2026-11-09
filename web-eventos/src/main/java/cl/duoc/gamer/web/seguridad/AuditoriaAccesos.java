package cl.duoc.gamer.web.seguridad;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

// Deja registro de los inicios de sesion correctos, fallidos y de los cierres de sesion (OWASP A09).
// Nunca escribe la clave ni el token, solo el usuario, la IP y el motivo del rechazo.
@Slf4j
@Component
public class AuditoriaAccesos {

    @EventListener
    public void loginCorrecto(AuthenticationSuccessEvent evento) {
        log.info("Login correcto: usuario={} ip={}", usuario(evento.getAuthentication()), ip(evento.getAuthentication()));
    }

    @EventListener
    public void loginFallido(AbstractAuthenticationFailureEvent evento) {
        log.warn("Login fallido: usuario={} ip={} motivo={}", usuario(evento.getAuthentication()),
                ip(evento.getAuthentication()), evento.getException().getClass().getSimpleName());
    }

    @EventListener
    public void cierreSesion(LogoutSuccessEvent evento) {
        log.info("Cierre de sesion: usuario={} ip={}", usuario(evento.getAuthentication()), ip(evento.getAuthentication()));
    }

    private String usuario(Authentication authentication) {
        String nombre = authentication == null ? "-" : authentication.getName();
        // Reemplaza saltos de linea y cualquier caracter de control que venga en el usuario escrito en el login
        String limpio = nombre.replaceAll("\\p{Cntrl}", "_");
        return limpio.length() > 30 ? limpio.substring(0, 30) + "..." : limpio;
    }

    private String ip(Authentication authentication) {
        if (authentication != null && authentication.getDetails() instanceof WebAuthenticationDetails detalles) {
            return detalles.getRemoteAddress();
        }
        return "-";
    }
}
