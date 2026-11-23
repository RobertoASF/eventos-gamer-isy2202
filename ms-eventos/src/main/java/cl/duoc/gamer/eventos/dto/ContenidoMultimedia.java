package cl.duoc.gamer.eventos.dto;

import java.util.Arrays;
import java.util.Objects;

// Archivo de una foto o video, con el tipo que se detecto al recibirlo.
// equals y hashCode comparan el contenido del arreglo; toString no imprime los bytes del archivo
public record ContenidoMultimedia(String tipoContenido, byte[] contenido) {

    @Override
    public boolean equals(Object otro) {
        return otro instanceof ContenidoMultimedia archivo
                && Objects.equals(tipoContenido, archivo.tipoContenido)
                && Arrays.equals(contenido, archivo.contenido);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hashCode(tipoContenido) + Arrays.hashCode(contenido);
    }

    @Override
    public String toString() {
        return "ContenidoMultimedia[tipoContenido=" + tipoContenido + ", bytes="
                + (contenido == null ? 0 : contenido.length) + "]";
    }
}
