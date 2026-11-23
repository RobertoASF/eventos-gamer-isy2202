package cl.duoc.gamer.web.modelo;

import java.util.Optional;

// Redes sociales en las que se puede compartir un evento. Lista cerrada: el valor que llega en la URL
// se busca aqui y cualquier otro se rechaza
public enum RedSocial {
    FACEBOOK("Facebook"),
    X("X"),
    WHATSAPP("WhatsApp"),
    TELEGRAM("Telegram"),
    LINKEDIN("LinkedIn");

    private final String nombre;

    RedSocial(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public static Optional<RedSocial> buscar(String valor) {
        for (RedSocial red : values()) {
            if (red.name().equalsIgnoreCase(valor)) {
                return Optional.of(red);
            }
        }
        return Optional.empty();
    }
}
