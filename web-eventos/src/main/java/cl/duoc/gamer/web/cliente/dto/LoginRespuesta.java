package cl.duoc.gamer.web.cliente.dto;

public record LoginRespuesta(String token, String tipo, long expiraEnSegundos, UsuarioDatos usuario) {

    @Override
    public String toString() {
        return "LoginRespuesta[usuario=" + usuario + "]";
    }
}
