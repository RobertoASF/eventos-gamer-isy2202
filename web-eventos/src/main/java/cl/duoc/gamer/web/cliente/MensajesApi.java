package cl.duoc.gamer.web.cliente;

import cl.duoc.gamer.web.cliente.dto.ErrorApi;
import org.springframework.web.client.HttpClientErrorException;

// Convierte un rechazo de la API (400, 403, 413) en el mensaje que se le muestra al usuario
public final class MensajesApi {

    private static final String GENERICO = "No pudimos guardar los cambios. Revisa los datos e intenta de nuevo.";

    private MensajesApi() {
    }

    public static String de(HttpClientErrorException ex) {
        if (ex.getStatusCode().value() == 413) {
            return "El archivo supera el tamaño máximo permitido.";
        }
        ErrorApi error;
        try {
            error = ex.getResponseBodyAs(ErrorApi.class);
        } catch (RuntimeException sinCuerpo) {
            return GENERICO;
        }
        if (error == null) {
            return GENERICO;
        }
        if (error.detalles() != null && !error.detalles().isEmpty()) {
            return error.detalles().values().iterator().next();
        }
        return error.mensaje() != null ? error.mensaje() : GENERICO;
    }
}
