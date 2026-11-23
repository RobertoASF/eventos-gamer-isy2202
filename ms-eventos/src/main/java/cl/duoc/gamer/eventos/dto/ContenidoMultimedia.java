package cl.duoc.gamer.eventos.dto;

// Archivo de una foto o video, con el tipo que se detecto al recibirlo
public record ContenidoMultimedia(String tipoContenido, byte[] contenido) {
}
