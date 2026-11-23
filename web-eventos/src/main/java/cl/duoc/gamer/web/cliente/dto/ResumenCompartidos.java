package cl.duoc.gamer.web.cliente.dto;

import java.util.Map;

public record ResumenCompartidos(long total, Map<String, Long> porDestino, boolean compartidoEnSitio) {
}
