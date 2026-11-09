package cl.duoc.gamer.usuarios.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Cuenta los intentos fallidos por usuario y bloquea el login por un tiempo al llegar al maximo.
// Frena los ataques de fuerza bruta contra una cuenta (OWASP A07).
// Los datos viven en memoria: si el servicio se reinicia los contadores vuelven a cero.
@Service
public class IntentosLoginService {

    public static final int MAXIMO_FALLIDOS = 5;
    public static final Duration VENTANA = Duration.ofMinutes(15);
    public static final Duration BLOQUEO = Duration.ofMinutes(15);

    // Sobre este tamano se limpian los registros vencidos para que el mapa no crezca sin limite
    private static final int LIMPIAR_SOBRE = 10_000;

    private final Map<String, Registro> registros = new ConcurrentHashMap<>();
    private final Clock reloj;

    @Autowired
    public IntentosLoginService() {
        this(Clock.systemUTC());
    }

    IntentosLoginService(Clock reloj) {
        this.reloj = reloj;
    }

    public boolean estaBloqueado(String nombreUsuario) {
        Registro registro = registros.get(clave(nombreUsuario));
        return registro != null && registro.bloqueadoHasta() != null
                && registro.bloqueadoHasta().isAfter(reloj.instant());
    }

    public void registrarFallido(String nombreUsuario) {
        Instant ahora = reloj.instant();
        registros.compute(clave(nombreUsuario), (llave, actual) -> {
            Registro registro = actual == null || actual.vencido(ahora) ? new Registro(0, ahora, null) : actual;
            int fallidos = registro.fallidos() + 1;
            Instant bloqueadoHasta = fallidos >= MAXIMO_FALLIDOS ? ahora.plus(BLOQUEO) : null;
            return new Registro(fallidos, registro.desde(), bloqueadoHasta);
        });
        if (registros.size() > LIMPIAR_SOBRE) {
            registros.values().removeIf(registro -> registro.vencido(ahora));
        }
    }

    public void registrarExitoso(String nombreUsuario) {
        registros.remove(clave(nombreUsuario));
    }

    private String clave(String nombreUsuario) {
        return nombreUsuario.trim().toLowerCase(Locale.ROOT);
    }

    private record Registro(int fallidos, Instant desde, Instant bloqueadoHasta) {

        // La ventana ya paso y, si hubo bloqueo, tambien termino
        boolean vencido(Instant ahora) {
            boolean ventanaTerminada = desde.plus(VENTANA).isBefore(ahora);
            boolean sinBloqueo = bloqueadoHasta == null || bloqueadoHasta.isBefore(ahora);
            return ventanaTerminada && sinBloqueo;
        }
    }
}
