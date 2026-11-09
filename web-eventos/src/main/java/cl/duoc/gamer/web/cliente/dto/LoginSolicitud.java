package cl.duoc.gamer.web.cliente.dto;

public record LoginSolicitud(String nombreUsuario, String password) {

    // Evita que la clave aparezca si el objeto llega a imprimirse
    @Override
    public String toString() {
        return "LoginSolicitud[nombreUsuario=" + nombreUsuario + "]";
    }
}
