package cl.duoc.gamer.web.seguridad;

import java.io.Serializable;
import org.springframework.security.core.AuthenticatedPrincipal;

// Datos del usuario que quedan guardados en su sesion, incluido el token para llamar a las API privadas.
// Cada sesion tiene el suyo, asi un usuario nunca usa el token de otro.
public record UsuarioSesion(String nombreUsuario, String nombre, String rol, String token)
        implements AuthenticatedPrincipal, Serializable {

    @Override
    public String getName() {
        return nombreUsuario;
    }

    // No incluye el token para que no termine escrito en un log
    @Override
    public String toString() {
        return "UsuarioSesion[nombreUsuario=" + nombreUsuario + ", rol=" + rol + "]";
    }
}
