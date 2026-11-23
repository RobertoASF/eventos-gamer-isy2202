package cl.duoc.gamer.eventos.service;

import cl.duoc.gamer.eventos.exception.SolicitudInvalidaException;
import cl.duoc.gamer.eventos.model.TipoMultimedia;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

// Revisa las fotos y videos que suben los usuarios. El tipo se reconoce por la firma del archivo
// (sus primeros bytes), porque la extension y el Content-Type los puede cambiar cualquiera
@Component
public class ValidadorMultimedia {

    static final int MAXIMO_FOTO = 5 * 1024 * 1024;
    static final int MAXIMO_VIDEO = 7 * 1024 * 1024;
    static final int LADO_MAXIMO = 6000;

    private static final byte[] FIRMA_JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] FIRMA_PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    private static final byte[] FIRMA_EBML = {0x1A, 0x45, (byte) 0xDF, (byte) 0xA3};
    private static final byte[] DOCTYPE_WEBM = "webm".getBytes(StandardCharsets.US_ASCII);

    // Marcas de MP4 aceptadas. Se dejan fuera las de QuickTime, HEIC y AVIF, que usan la misma estructura
    private static final Set<String> MARCAS_MP4 = Set.of("isom", "iso2", "iso4", "iso5", "iso6", "mp41", "mp42",
            "avc1", "dash", "M4V ", "mmp4");

    public ArchivoValidado validar(byte[] datos) {
        if (datos == null || datos.length == 0) {
            throw new SolicitudInvalidaException("Debes adjuntar una foto o un video");
        }
        if (empiezaCon(datos, FIRMA_JPEG)) {
            return foto(datos, "jpeg", MediaType.IMAGE_JPEG_VALUE);
        }
        if (empiezaCon(datos, FIRMA_PNG)) {
            return foto(datos, "png", MediaType.IMAGE_PNG_VALUE);
        }
        if (esMp4(datos)) {
            return video(datos, "video/mp4");
        }
        if (esWebm(datos)) {
            return video(datos, "video/webm");
        }
        throw new SolicitudInvalidaException("Solo se aceptan fotos JPG o PNG y videos MP4 o WebM");
    }

    private ArchivoValidado foto(byte[] datos, String formato, String tipoContenido) {
        if (datos.length > MAXIMO_FOTO) {
            throw new SolicitudInvalidaException("Las fotos pueden pesar hasta 5 MB");
        }
        return new ArchivoValidado(TipoMultimedia.FOTO, tipoContenido, recodificar(datos, formato));
    }

    private ArchivoValidado video(byte[] datos, String tipoContenido) {
        if (datos.length > MAXIMO_VIDEO) {
            throw new SolicitudInvalidaException("Los videos pueden pesar hasta 7 MB");
        }
        return new ArchivoValidado(TipoMultimedia.VIDEO, tipoContenido, datos);
    }

    // La imagen se vuelve a generar desde sus pixeles. Asi se descartan los metadatos (por ejemplo la ubicacion
    // GPS que guardan los celulares) y cualquier contenido extra escondido dentro del archivo
    private byte[] recodificar(byte[] datos, String formato) {
        try (ImageInputStream entrada = ImageIO.createImageInputStream(new ByteArrayInputStream(datos))) {
            Iterator<ImageReader> lectores = ImageIO.getImageReaders(entrada);
            if (!lectores.hasNext()) {
                throw fotoIlegible();
            }
            ImageReader lector = lectores.next();
            try {
                lector.setInput(entrada, true, true);
                // Se revisan las dimensiones antes de leer los pixeles, para no reservar memoria de mas
                if (lector.getWidth(0) > LADO_MAXIMO || lector.getHeight(0) > LADO_MAXIMO) {
                    throw new SolicitudInvalidaException("Las fotos pueden medir hasta 6000 x 6000 píxeles");
                }
                BufferedImage imagen = lector.read(0);
                BufferedImage limpia = "jpeg".equals(formato) ? sinTransparencia(imagen) : imagen;
                ByteArrayOutputStream salida = new ByteArrayOutputStream();
                if (!ImageIO.write(limpia, formato, salida)) {
                    throw fotoIlegible();
                }
                return salida.toByteArray();
            } finally {
                lector.dispose();
            }
        } catch (IOException | RuntimeException ex) {
            if (ex instanceof SolicitudInvalidaException invalida) {
                throw invalida;
            }
            throw fotoIlegible();
        }
    }

    private BufferedImage sinTransparencia(BufferedImage imagen) {
        if (imagen.getType() == BufferedImage.TYPE_3BYTE_BGR || imagen.getType() == BufferedImage.TYPE_INT_RGB) {
            return imagen;
        }
        BufferedImage rgb = new BufferedImage(imagen.getWidth(), imagen.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D lienzo = rgb.createGraphics();
        try {
            lienzo.drawImage(imagen, 0, 0, Color.WHITE, null);
        } finally {
            lienzo.dispose();
        }
        return rgb;
    }

    private SolicitudInvalidaException fotoIlegible() {
        return new SolicitudInvalidaException("La foto está dañada o no se pudo leer");
    }

    // MP4: la caja "ftyp" va en los bytes 4 a 7 y despues viene la marca del formato
    private boolean esMp4(byte[] datos) {
        if (datos.length < 12) {
            return false;
        }
        String caja = new String(datos, 4, 4, StandardCharsets.US_ASCII);
        String marca = new String(datos, 8, 4, StandardCharsets.US_ASCII);
        return "ftyp".equals(caja) && MARCAS_MP4.contains(marca);
    }

    // WebM: cabecera EBML con el tipo de documento "webm" (Matroska usa la misma cabecera con otro tipo)
    private boolean esWebm(byte[] datos) {
        if (!empiezaCon(datos, FIRMA_EBML)) {
            return false;
        }
        byte[] cabecera = Arrays.copyOf(datos, Math.min(datos.length, 64));
        for (int i = 0; i <= cabecera.length - DOCTYPE_WEBM.length; i++) {
            if (Arrays.equals(cabecera, i, i + DOCTYPE_WEBM.length, DOCTYPE_WEBM, 0, DOCTYPE_WEBM.length)) {
                return true;
            }
        }
        return false;
    }

    private boolean empiezaCon(byte[] datos, byte[] firma) {
        return datos.length >= firma.length && Arrays.equals(datos, 0, firma.length, firma, 0, firma.length);
    }
}
