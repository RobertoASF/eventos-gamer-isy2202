package cl.duoc.gamer.eventos.security;

import org.springframework.security.oauth2.jwt.Jwt;

// Datos del usuario que vienen dentro del token ya validado (firma, emisor y expiracion).
// El autor de un comentario o de una foto siempre sale de aqui, nunca de un parametro de la peticion
public record UsuarioToken(Long id, String nombreUsuario, String nombre, boolean admin) {

    public static UsuarioToken desde(Jwt jwt) {
        Object uid = jwt.getClaim("uid");
        if (!(uid instanceof Number numero)) {
            throw new IllegalStateException("El token no trae el id del usuario");
        }
        String nombre = jwt.getClaimAsString("nombre");
        return new UsuarioToken(numero.longValue(), jwt.getSubject(),
                nombre == null || nombre.isBlank() ? jwt.getSubject() : nombre,
                "ADMIN".equals(jwt.getClaimAsString("rol")));
    }

    // Cada usuario puede eliminar lo que publico; el administrador puede moderar cualquier publicacion
    public boolean puedeEliminar(Long autorId) {
        return admin || id.equals(autorId);
    }
}
