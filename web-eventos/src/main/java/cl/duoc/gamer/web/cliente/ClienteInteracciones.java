package cl.duoc.gamer.web.cliente;

import cl.duoc.gamer.web.cliente.dto.Compartido;
import cl.duoc.gamer.web.cliente.dto.Multimedia;
import cl.duoc.gamer.web.cliente.dto.Opiniones;
import cl.duoc.gamer.web.cliente.dto.ResumenCompartidos;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

// Funcionalidades privadas de la semana 5 en ms-eventos: fotos y videos, opiniones y compartidos.
// Todas las llamadas envian el token del usuario que tiene la sesion
@Component
public class ClienteInteracciones {

    private static final String RUTA_MULTIMEDIA = "/api/eventos/{eventoId}/multimedia";
    private static final String RUTA_COMENTARIOS = "/api/eventos/{eventoId}/comentarios";
    private static final String RUTA_COMPARTIDOS = "/api/eventos/{eventoId}/compartidos";
    private static final String PREFIJO_TOKEN = "Bearer ";

    private final RestClient restClient;
    private final RestClient restArchivos;

    public ClienteInteracciones(RestClient.Builder builder, @Value("${servicios.eventos.url}") String urlBase) {
        this.restClient = FabricaClientes.crear(builder, urlBase);
        this.restArchivos = FabricaClientes.crear(builder, urlBase, Duration.ofSeconds(30));
    }

    public List<Multimedia> multimedia(Long eventoId, String token) {
        return restClient.get()
                .uri(RUTA_MULTIMEDIA, eventoId)
                .header(HttpHeaders.AUTHORIZATION, PREFIJO_TOKEN + token)
                .retrieve()
                .body(new ParameterizedTypeReference<>() { });
    }

    // El archivo se reenvia con un nombre fijo: el nombre original lo eligio el usuario y no se usa para nada
    public void subirMultimedia(Long eventoId, String token, MultipartFile archivo, String descripcion)
            throws IOException {
        HttpHeaders cabeceraArchivo = new HttpHeaders();
        cabeceraArchivo.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        cabeceraArchivo.setContentDisposition(ContentDisposition.formData().name("archivo").filename("archivo").build());
        MultiValueMap<String, Object> partes = new LinkedMultiValueMap<>();
        partes.add("archivo", new HttpEntity<>(new ByteArrayResource(archivo.getBytes()), cabeceraArchivo));
        partes.add("descripcion", descripcion == null ? "" : descripcion);
        restArchivos.post()
                .uri(RUTA_MULTIMEDIA, eventoId)
                .header(HttpHeaders.AUTHORIZATION, PREFIJO_TOKEN + token)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(partes)
                .retrieve()
                .toBodilessEntity();
    }

    // Reenvia el encabezado Range para que el navegador pueda adelantar los videos
    public ResponseEntity<byte[]> contenidoMultimedia(Long eventoId, Long id, String token, String rango) {
        return restArchivos.get()
                .uri(RUTA_MULTIMEDIA + "/{id}/contenido", eventoId, id)
                .header(HttpHeaders.AUTHORIZATION, PREFIJO_TOKEN + token)
                .headers(cabeceras -> {
                    if (rango != null) {
                        cabeceras.set(HttpHeaders.RANGE, rango);
                    }
                })
                .retrieve()
                .toEntity(byte[].class);
    }

    public void eliminarMultimedia(Long eventoId, Long id, String token) {
        restClient.delete()
                .uri(RUTA_MULTIMEDIA + "/{id}", eventoId, id)
                .header(HttpHeaders.AUTHORIZATION, PREFIJO_TOKEN + token)
                .retrieve()
                .toBodilessEntity();
    }

    public Opiniones opiniones(Long eventoId, String token) {
        return restClient.get()
                .uri(RUTA_COMENTARIOS, eventoId)
                .header(HttpHeaders.AUTHORIZATION, PREFIJO_TOKEN + token)
                .retrieve()
                .body(Opiniones.class);
    }

    public void guardarOpinion(Long eventoId, String token, int puntuacion, String texto) {
        restClient.post()
                .uri(RUTA_COMENTARIOS, eventoId)
                .header(HttpHeaders.AUTHORIZATION, PREFIJO_TOKEN + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("puntuacion", puntuacion, "texto", texto))
                .retrieve()
                .toBodilessEntity();
    }

    public void eliminarOpinion(Long eventoId, Long id, String token) {
        restClient.delete()
                .uri(RUTA_COMENTARIOS + "/{id}", eventoId, id)
                .header(HttpHeaders.AUTHORIZATION, PREFIJO_TOKEN + token)
                .retrieve()
                .toBodilessEntity();
    }

    public ResumenCompartidos compartidos(Long eventoId, String token) {
        return restClient.get()
                .uri(RUTA_COMPARTIDOS, eventoId)
                .header(HttpHeaders.AUTHORIZATION, PREFIJO_TOKEN + token)
                .retrieve()
                .body(ResumenCompartidos.class);
    }

    public void compartir(Long eventoId, String token, String destino, String mensaje) {
        restClient.post()
                .uri(RUTA_COMPARTIDOS, eventoId)
                .header(HttpHeaders.AUTHORIZATION, PREFIJO_TOKEN + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("destino", destino, "mensaje", mensaje == null ? "" : mensaje))
                .retrieve()
                .toBodilessEntity();
    }

    public List<Compartido> comunidad(String token) {
        return restClient.get()
                .uri("/api/compartidos?limite={limite}", 20)
                .header(HttpHeaders.AUTHORIZATION, PREFIJO_TOKEN + token)
                .retrieve()
                .body(new ParameterizedTypeReference<>() { });
    }
}
