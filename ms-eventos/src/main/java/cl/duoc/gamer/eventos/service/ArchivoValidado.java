package cl.duoc.gamer.eventos.service;

import cl.duoc.gamer.eventos.model.TipoMultimedia;
import java.util.Arrays;
import java.util.Objects;

// Archivo que ya paso la validacion: su tipo se detecto por el contenido y no por el nombre ni por lo que
// declaro el navegador. equals y hashCode comparan el contenido del arreglo; toString no imprime los bytes
public record ArchivoValidado(TipoMultimedia tipo, String tipoContenido, byte[] contenido) {

    @Override
    public boolean equals(Object otro) {
        return otro instanceof ArchivoValidado archivo
                && tipo == archivo.tipo
                && Objects.equals(tipoContenido, archivo.tipoContenido)
                && Arrays.equals(contenido, archivo.contenido);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, tipoContenido) * 31 + Arrays.hashCode(contenido);
    }

    @Override
    public String toString() {
        return "ArchivoValidado[tipo=" + tipo + ", tipoContenido=" + tipoContenido + ", bytes="
                + (contenido == null ? 0 : contenido.length) + "]";
    }
}
