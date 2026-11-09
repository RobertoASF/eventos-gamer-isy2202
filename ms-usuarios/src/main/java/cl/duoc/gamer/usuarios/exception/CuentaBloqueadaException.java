package cl.duoc.gamer.usuarios.exception;

public class CuentaBloqueadaException extends RuntimeException {

    public CuentaBloqueadaException() {
        super("Demasiados intentos fallidos. Espera unos minutos antes de volver a intentarlo");
    }
}
