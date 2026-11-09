package cl.duoc.gamer.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

// Muestra una pagina de error propia, sin detalles internos, cuando falla una llamada a las API
@Slf4j
@ControllerAdvice
public class ManejadorErroresWeb {

    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String noEncontrado(Model model) {
        return pagina(model, HttpStatus.NOT_FOUND, "El evento que buscas no existe o ya no está publicado.");
    }

    // La API rechazo el token (vencio o no es valido): se cierra la sesion y se pide iniciarla de nuevo
    @ExceptionHandler(HttpClientErrorException.Unauthorized.class)
    public String tokenRechazado(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        SecurityContextHolder.clearContext();
        log.info("Sesion cerrada porque la API rechazo el token del usuario");
        return "redirect:/login?expirada";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String parametroInvalido(Model model) {
        return pagina(model, HttpStatus.BAD_REQUEST, "La dirección que abriste no es válida.");
    }

    @ExceptionHandler(RestClientException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String servicioNoDisponible(RestClientException ex, Model model) {
        log.error("No se pudo completar la llamada a una API: {}", ex.getMessage());
        return pagina(model, HttpStatus.SERVICE_UNAVAILABLE,
                "No pudimos cargar la información en este momento. Intenta de nuevo en unos minutos.");
    }

    private String pagina(Model model, HttpStatus estado, String mensaje) {
        model.addAttribute("status", estado.value());
        model.addAttribute("error", estado.getReasonPhrase());
        model.addAttribute("mensaje", mensaje);
        return "error";
    }
}
