package cl.duoc.gamer.web.cliente;

import java.time.Duration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

final class FabricaClientes {

    private FabricaClientes() {
    }

    // Si una API no responde, la pagina falla rapido en vez de quedar esperando
    static RestClient crear(RestClient.Builder builder, String urlBase) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(3));
        fabrica.setReadTimeout(Duration.ofSeconds(5));
        return builder.clone()
                .baseUrl(urlBase)
                .requestFactory(fabrica)
                .build();
    }
}
