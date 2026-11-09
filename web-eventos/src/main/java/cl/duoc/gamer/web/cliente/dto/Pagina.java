package cl.duoc.gamer.web.cliente.dto;

import java.util.List;

public record Pagina<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas) {

    public boolean tieneAnterior() {
        return pagina > 0;
    }

    public boolean tieneSiguiente() {
        return pagina + 1 < totalPaginas;
    }
}
