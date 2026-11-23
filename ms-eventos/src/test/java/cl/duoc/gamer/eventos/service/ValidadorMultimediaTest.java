package cl.duoc.gamer.eventos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cl.duoc.gamer.eventos.exception.SolicitudInvalidaException;
import cl.duoc.gamer.eventos.model.TipoMultimedia;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class ValidadorMultimediaTest {

    private final ValidadorMultimedia validador = new ValidadorMultimedia();

    @Test
    void aceptaPngYLoVuelveAGenerar() throws IOException {
        ArchivoValidado resultado = validador.validar(imagen("png", 40, 30));

        assertThat(resultado.tipo()).isEqualTo(TipoMultimedia.FOTO);
        assertThat(resultado.tipoContenido()).isEqualTo("image/png");
        assertThat(ImageIO.read(new java.io.ByteArrayInputStream(resultado.contenido())).getWidth()).isEqualTo(40);
    }

    @Test
    void aceptaJpegSinConservarDatosAgregadosAlFinal() throws IOException {
        byte[] jpeg = imagen("jpeg", 20, 20);
        byte[] conExtra = concatenar(jpeg, "<script>alert(1)</script>".getBytes(StandardCharsets.US_ASCII));

        ArchivoValidado resultado = validador.validar(conExtra);

        assertThat(resultado.tipoContenido()).isEqualTo("image/jpeg");
        assertThat(new String(resultado.contenido(), StandardCharsets.ISO_8859_1)).doesNotContain("<script>");
    }

    @Test
    void rechazaUnHtmlConExtensionDeImagen() {
        byte[] html = "<html><script>alert(document.cookie)</script></html>".getBytes(StandardCharsets.UTF_8);

        assertThatThrownBy(() -> validador.validar(html))
                .isInstanceOf(SolicitudInvalidaException.class)
                .hasMessageContaining("Solo se aceptan");
    }

    @Test
    void rechazaUnaImagenDanadaAunqueTengaLaFirmaCorrecta() {
        byte[] falsa = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x01, 0x02};

        assertThatThrownBy(() -> validador.validar(falsa))
                .isInstanceOf(SolicitudInvalidaException.class)
                .hasMessageContaining("dañada");
    }

    @Test
    void rechazaFotosMasGrandesQueElLimite() {
        byte[] grande = new byte[ValidadorMultimedia.MAXIMO_FOTO + 1];
        grande[0] = (byte) 0x89;
        System.arraycopy(new byte[] {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'}, 0, grande, 0, 8);

        assertThatThrownBy(() -> validador.validar(grande))
                .isInstanceOf(SolicitudInvalidaException.class)
                .hasMessageContaining("5 MB");
    }

    @Test
    void reconoceVideosMp4YWebm() {
        byte[] mp4 = concatenar(new byte[] {0, 0, 0, 0x18}, "ftypisom".getBytes(StandardCharsets.US_ASCII));
        byte[] webm = concatenar(new byte[] {0x1A, 0x45, (byte) 0xDF, (byte) 0xA3, 0x42, (byte) 0x82, (byte) 0x84},
                "webm".getBytes(StandardCharsets.US_ASCII));

        assertThat(validador.validar(mp4).tipoContenido()).isEqualTo("video/mp4");
        assertThat(validador.validar(webm).tipoContenido()).isEqualTo("video/webm");
    }

    @Test
    void rechazaQuickTimeAunqueUseLaMismaEstructuraQueMp4() {
        byte[] mov = concatenar(new byte[] {0, 0, 0, 0x14}, "ftypqt  ".getBytes(StandardCharsets.US_ASCII));

        assertThatThrownBy(() -> validador.validar(mov)).isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void rechazaArchivoVacio() {
        assertThatThrownBy(() -> validador.validar(new byte[0])).isInstanceOf(SolicitudInvalidaException.class);
    }

    private byte[] imagen(String formato, int ancho, int alto) throws IOException {
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(imagen, formato, salida);
        return salida.toByteArray();
    }

    private byte[] concatenar(byte[] primero, byte[] segundo) {
        byte[] resultado = Arrays.copyOf(primero, primero.length + segundo.length);
        System.arraycopy(segundo, 0, resultado, primero.length, segundo.length);
        return resultado;
    }
}
