package cl.duoc.gamer.usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IntentosLoginServiceTest {

    private RelojAjustable reloj;
    private IntentosLoginService intentos;

    @BeforeEach
    void preparar() {
        reloj = new RelojAjustable(Instant.parse("2026-11-09T12:00:00Z"));
        intentos = new IntentosLoginService(reloj);
    }

    @Test
    void noBloqueaAntesDelMaximoDeIntentos() {
        for (int i = 1; i < IntentosLoginService.MAXIMO_FALLIDOS; i++) {
            intentos.registrarFallido("mati");
        }
        assertThat(intentos.estaBloqueado("mati")).isFalse();
    }

    @Test
    void bloqueaAlLlegarAlMaximoDeIntentos() {
        for (int i = 0; i < IntentosLoginService.MAXIMO_FALLIDOS; i++) {
            intentos.registrarFallido("mati");
        }
        assertThat(intentos.estaBloqueado("mati")).isTrue();
        assertThat(intentos.estaBloqueado("vale")).isFalse();
    }

    @Test
    void elBloqueoNoDistingueMayusculas() {
        for (int i = 0; i < IntentosLoginService.MAXIMO_FALLIDOS; i++) {
            intentos.registrarFallido(i % 2 == 0 ? "MATI" : " mati ");
        }
        assertThat(intentos.estaBloqueado("Mati")).isTrue();
    }

    @Test
    void elBloqueoTerminaDespuesDelTiempoDefinido() {
        for (int i = 0; i < IntentosLoginService.MAXIMO_FALLIDOS; i++) {
            intentos.registrarFallido("mati");
        }
        reloj.avanzar(IntentosLoginService.BLOQUEO.plusSeconds(1));
        assertThat(intentos.estaBloqueado("mati")).isFalse();
    }

    @Test
    void unLoginCorrectoReiniciaElContador() {
        for (int i = 1; i < IntentosLoginService.MAXIMO_FALLIDOS; i++) {
            intentos.registrarFallido("mati");
        }
        intentos.registrarExitoso("mati");
        intentos.registrarFallido("mati");
        assertThat(intentos.estaBloqueado("mati")).isFalse();
    }

    @Test
    void losFallosAntiguosNoSeAcumulan() {
        for (int i = 1; i < IntentosLoginService.MAXIMO_FALLIDOS; i++) {
            intentos.registrarFallido("mati");
        }
        reloj.avanzar(IntentosLoginService.VENTANA.plusMinutes(1));
        intentos.registrarFallido("mati");
        assertThat(intentos.estaBloqueado("mati")).isFalse();
    }

    private static final class RelojAjustable extends Clock {

        private Instant ahora;

        RelojAjustable(Instant inicio) {
            this.ahora = inicio;
        }

        void avanzar(Duration tiempo) {
            ahora = ahora.plus(tiempo);
        }

        @Override
        public Instant instant() {
            return ahora;
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zona) {
            return this;
        }
    }
}
