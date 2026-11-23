package cl.duoc.gamer.eventos.exception;

// Datos que no cumplen una regla del negocio (archivo no permitido, limite alcanzado, etc.). Responde 400
public class SolicitudInvalidaException extends RuntimeException {

    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
