package cl.duoc.gamer.web.cliente.dto;

import java.util.Map;

// Cuerpo de los errores que responden las API (400, 403, 404)
public record ErrorApi(int estado, String mensaje, Map<String, String> detalles) {
}
