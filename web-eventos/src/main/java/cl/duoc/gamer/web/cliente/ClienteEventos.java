package cl.duoc.gamer.web.cliente;

import cl.duoc.gamer.web.cliente.dto.Banner;
import cl.duoc.gamer.web.cliente.dto.EventoDetalle;
import cl.duoc.gamer.web.cliente.dto.EventoResumen;
import cl.duoc.gamer.web.cliente.dto.Filtros;
import cl.duoc.gamer.web.cliente.dto.Pagina;
import cl.duoc.gamer.web.modelo.FiltroBusqueda;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

@Component
public class ClienteEventos {

    public static final int TAMANO_PAGINA = 9;

    private final RestClient restClient;

    public ClienteEventos(RestClient.Builder builder, @Value("${servicios.eventos.url}") String urlBase) {
        this.restClient = FabricaClientes.crear(builder, urlBase);
    }

    public List<EventoResumen> recientes() {
        return restClient.get()
                .uri("/api/eventos/recientes?limite={limite}", 6)
                .retrieve()
                .body(new ParameterizedTypeReference<>() { });
    }

    public List<EventoResumen> populares() {
        return restClient.get()
                .uri("/api/eventos/populares?limite={limite}", 4)
                .retrieve()
                .body(new ParameterizedTypeReference<>() { });
    }

    public List<Banner> banners() {
        return restClient.get()
                .uri("/api/banners")
                .retrieve()
                .body(new ParameterizedTypeReference<>() { });
    }

    public Filtros filtros() {
        return restClient.get()
                .uri("/api/eventos/filtros")
                .retrieve()
                .body(Filtros.class);
    }

    public Pagina<EventoResumen> buscar(FiltroBusqueda filtro, int pagina) {
        return restClient.get()
                .uri(uri -> uriBusqueda(uri, filtro, pagina))
                .retrieve()
                .body(new ParameterizedTypeReference<>() { });
    }

    // Ruta privada de la API: se envia el token que el usuario obtuvo al iniciar sesion
    public EventoDetalle detalle(Long id, String token) {
        return restClient.get()
                .uri("/api/eventos/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .body(EventoDetalle.class);
    }

    // Los valores escritos por el usuario van como variables de la URI para que se codifiquen completos
    private URI uriBusqueda(UriBuilder uri, FiltroBusqueda filtro, int pagina) {
        Map<String, Object> valores = new LinkedHashMap<>();
        agregar(valores, "nombre", filtro.getNombre());
        agregar(valores, "videojuegoId", filtro.getVideojuegoId());
        agregar(valores, "categoriaId", filtro.getCategoriaId());
        agregar(valores, "pais", filtro.getPais());
        agregar(valores, "ciudad", filtro.getCiudad());
        agregar(valores, "modalidad", filtro.getModalidad());
        agregar(valores, "desde", filtro.getDesde());
        agregar(valores, "hasta", filtro.getHasta());
        valores.put("pagina", pagina);
        valores.put("tamano", TAMANO_PAGINA);

        uri.path("/api/eventos/buscar");
        valores.keySet().forEach(clave -> uri.queryParam(clave, "{" + clave + "}"));
        return uri.build(valores);
    }

    private void agregar(Map<String, Object> valores, String clave, Object valor) {
        if (valor == null || valor instanceof String texto && texto.isBlank()) {
            return;
        }
        valores.put(clave, valor instanceof String texto ? texto.trim() : valor.toString());
    }
}
