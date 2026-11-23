package cl.duoc.gamer.eventos.exception;

import cl.duoc.gamer.eventos.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class ManejadorErrores extends ResponseEntityExceptionHandler {

    private static final String DATOS_INVALIDOS = "Los datos enviados no son válidos";
    private static final String FORMATO_INVALIDO = "El valor no tiene un formato válido";

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Object> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<Object> invalida(SolicitudInvalidaException ex, HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(AccionNoPermitidaException.class)
    public ResponseEntity<Object> noPermitida(AccionNoPermitidaException ex, HttpServletRequest request) {
        return responder(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> inesperado(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en {}", request.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno", request.getRequestURI());
    }

    // Los filtros de busqueda y los cuerpos JSON se validan campo por campo
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> detalles = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            String mensaje = error.isBindingFailure() ? FORMATO_INVALIDO : error.getDefaultMessage();
            detalles.putIfAbsent(error.getField(), mensaje);
        }
        ErrorResponse cuerpo = ErrorResponse.de(status, DATOS_INVALIDOS, ruta(request), detalles);
        return ResponseEntity.status(status).headers(headers).body(cuerpo);
    }

    // Parametros sueltos con restricciones (limite, descripcion de una foto, etc.)
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> detalles = new LinkedHashMap<>();
        ex.getParameterValidationResults().forEach(resultado -> resultado.getResolvableErrors().stream()
                .findFirst()
                .ifPresent(error -> detalles.putIfAbsent(resultado.getMethodParameter().getParameterName(),
                        error.getDefaultMessage())));
        ErrorResponse cuerpo = ErrorResponse.de(status, DATOS_INVALIDOS, ruta(request), detalles);
        return ResponseEntity.status(status).headers(headers).body(cuerpo);
    }

    // Mismo formato JSON para los errores propios de Spring (404, 405, parametros invalidos, etc.)
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        ErrorResponse cuerpo = ErrorResponse.de(status, mensajePara(status), ruta(request));
        return ResponseEntity.status(status).headers(headers).body(cuerpo);
    }

    private String mensajePara(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "La solicitud no es válida";
            case 404 -> "El recurso solicitado no existe";
            case 405 -> "El método HTTP no está permitido en esta ruta";
            case 413 -> "El archivo supera el tamaño máximo permitido";
            case 415 -> "El tipo de contenido enviado no está permitido";
            default -> "No se pudo procesar la solicitud";
        };
    }

    private ResponseEntity<Object> responder(HttpStatus estado, String mensaje, String ruta) {
        return ResponseEntity.status(estado).body(ErrorResponse.de(estado, mensaje, ruta));
    }

    private String ruta(WebRequest request) {
        return ((ServletWebRequest) request).getRequest().getRequestURI();
    }
}
