package cl.duoc.gamer.web.modelo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

// Arma los enlaces para compartir un evento en redes sociales. La direccion publica del sitio viene de la
// configuracion y no del encabezado Host de la peticion, que lo puede cambiar quien hace la peticion
@Component
public class EnlacesCompartir {

    private final String urlPublica;

    public EnlacesCompartir(@Value("${sitio.url-publica}") String urlPublica) {
        if (!urlPublica.startsWith("https://")) {
            throw new IllegalStateException("sitio.url-publica debe ser una direccion https");
        }
        this.urlPublica = urlPublica.endsWith("/") ? urlPublica.substring(0, urlPublica.length() - 1) : urlPublica;
    }

    public String urlEvento(Long id) {
        return urlPublica + "/detalle/" + id;
    }

    public List<EnlaceRed> enlaces(Long id, String nombreEvento) {
        String url = urlEvento(id);
        String texto = "Mira este evento en Eventos Gamer: " + nombreEvento;
        List<EnlaceRed> enlaces = new ArrayList<>();
        enlaces.add(new EnlaceRed(RedSocial.FACEBOOK,
                construir("https://www.facebook.com/sharer/sharer.php", parametros("u", url))));
        enlaces.add(new EnlaceRed(RedSocial.X,
                construir("https://x.com/intent/post", parametros("text", texto, "url", url))));
        enlaces.add(new EnlaceRed(RedSocial.WHATSAPP,
                construir("https://api.whatsapp.com/send", parametros("text", texto + " " + url))));
        enlaces.add(new EnlaceRed(RedSocial.TELEGRAM,
                construir("https://t.me/share/url", parametros("url", url, "text", texto))));
        enlaces.add(new EnlaceRed(RedSocial.LINKEDIN,
                construir("https://www.linkedin.com/sharing/share-offsite/", parametros("url", url))));
        return enlaces;
    }

    // Los valores se codifican completos, asi el nombre del evento no puede agregar parametros a la URL
    private String construir(String base, Map<String, String> parametros) {
        UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(base);
        parametros.keySet().forEach(clave -> uri.queryParam(clave, "{" + clave + "}"));
        return uri.encode().buildAndExpand(parametros).toUriString();
    }

    private Map<String, String> parametros(String... claveValor) {
        Map<String, String> mapa = new LinkedHashMap<>();
        for (int i = 0; i < claveValor.length; i += 2) {
            mapa.put(claveValor[i], claveValor[i + 1]);
        }
        return mapa;
    }

    public record EnlaceRed(RedSocial red, String url) {
    }
}
