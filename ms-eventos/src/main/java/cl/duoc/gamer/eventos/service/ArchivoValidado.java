package cl.duoc.gamer.eventos.service;

import cl.duoc.gamer.eventos.model.TipoMultimedia;

// Archivo que ya paso la validacion: su tipo se detecto por el contenido y no por el nombre ni por lo que
// declaro el navegador
public record ArchivoValidado(TipoMultimedia tipo, String tipoContenido, byte[] contenido) {
}
