package cl.duoc.gamer.eventos.exception;

// El usuario tiene sesion pero intenta modificar algo que no le pertenece. Responde 403
public class AccionNoPermitidaException extends RuntimeException {

    public AccionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}
