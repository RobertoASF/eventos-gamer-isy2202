package cl.duoc.gamer.web.cliente;

import cl.duoc.gamer.web.cliente.dto.LoginRespuesta;
import cl.duoc.gamer.web.cliente.dto.LoginSolicitud;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ClienteUsuarios {

    private final RestClient restClient;

    public ClienteUsuarios(RestClient.Builder builder, @Value("${servicios.usuarios.url}") String urlBase) {
        this.restClient = FabricaClientes.crear(builder, urlBase);
    }

    // Devuelve vacio cuando la API rechaza las credenciales.
    // Si la cuenta esta bloqueada por intentos fallidos lanza CuentaBloqueadaException
    public Optional<LoginRespuesta> login(String nombreUsuario, String password) {
        TokenCsrf csrf = pedirTokenCsrf();
        try {
            LoginRespuesta respuesta = restClient.post()
                    .uri("/api/auth/login")
                    .header(csrf.cabecera(), csrf.token())
                    .header(HttpHeaders.COOKIE, csrf.cookie())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new LoginSolicitud(nombreUsuario, password))
                    .retrieve()
                    .body(LoginRespuesta.class);
            return Optional.ofNullable(respuesta);
        } catch (HttpClientErrorException.TooManyRequests ex) {
            throw new CuentaBloqueadaException();
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.BadRequest ex) {
            return Optional.empty();
        }
    }

    // ms-usuarios valida el token CSRF por doble envio: el mismo valor en la cookie y en el encabezado
    private TokenCsrf pedirTokenCsrf() {
        ResponseEntity<Map<String, String>> respuesta = restClient.get()
                .uri("/api/auth/csrf")
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() { });
        Map<String, String> cuerpo = respuesta.getBody();
        String cookie = respuesta.getHeaders().getOrEmpty(HttpHeaders.SET_COOKIE).stream()
                .filter(valor -> valor.startsWith("XSRF-TOKEN="))
                .map(valor -> valor.split(";", 2)[0])
                .findFirst()
                .orElseThrow(() -> new RestClientException("ms-usuarios no entrego la cookie del token CSRF"));
        if (cuerpo == null || cuerpo.get("cabecera") == null || cuerpo.get("token") == null) {
            throw new RestClientException("ms-usuarios no entrego el token CSRF");
        }
        return new TokenCsrf(cuerpo.get("cabecera"), cuerpo.get("token"), cookie);
    }

    private record TokenCsrf(String cabecera, String token, String cookie) {
    }

    public static class CuentaBloqueadaException extends RuntimeException {

        public CuentaBloqueadaException() {
            super("Cuenta bloqueada temporalmente por intentos fallidos");
        }
    }
}
