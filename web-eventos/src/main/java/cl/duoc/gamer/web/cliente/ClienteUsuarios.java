package cl.duoc.gamer.web.cliente;

import cl.duoc.gamer.web.cliente.dto.LoginRespuesta;
import cl.duoc.gamer.web.cliente.dto.LoginSolicitud;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class ClienteUsuarios {

    private final RestClient restClient;

    public ClienteUsuarios(RestClient.Builder builder, @Value("${servicios.usuarios.url}") String urlBase) {
        this.restClient = FabricaClientes.crear(builder, urlBase);
    }

    // Devuelve vacio cuando la API rechaza las credenciales
    public Optional<LoginRespuesta> login(String nombreUsuario, String password) {
        try {
            LoginRespuesta respuesta = restClient.post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new LoginSolicitud(nombreUsuario, password))
                    .retrieve()
                    .body(LoginRespuesta.class);
            return Optional.ofNullable(respuesta);
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.BadRequest ex) {
            return Optional.empty();
        }
    }
}
