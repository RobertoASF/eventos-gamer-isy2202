package cl.duoc.gamer.usuarios.dto;

import cl.duoc.gamer.usuarios.model.Usuario;

public record UsuarioResponse(Long id, String nombreUsuario, String nombre, String email, String rol) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombreUsuario(), usuario.getNombre(),
                usuario.getEmail(), usuario.getRol().getNombre());
    }
}
