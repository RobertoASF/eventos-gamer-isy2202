package cl.duoc.gamer.web.cliente.dto;

import java.time.LocalDateTime;
import java.util.List;

public record EventoDetalle(
        Long id,
        String nombre,
        String descripcion,
        String videojuego,
        String generoVideojuego,
        String categoria,
        String modalidad,
        String pais,
        String ciudad,
        String lugar,
        String enlaceAcceso,
        LocalDateTime fechaInicio,
        String organizador,
        String requisitos,
        String inscripcion,
        int valorEntrada,
        int cupos,
        int interesados,
        String imagenPortada,
        List<Foto> fotos) {

    public boolean esOnline() {
        return "ONLINE".equals(modalidad);
    }

    // Solo se muestra como enlace si es https, asi un valor como javascript: nunca llega al href
    public boolean tieneEnlaceSeguro() {
        return enlaceAcceso != null && enlaceAcceso.startsWith("https://");
    }
}
