package cl.duoc.gamer.eventos.dto;

import cl.duoc.gamer.eventos.model.Modalidad;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

// Todos los filtros son opcionales, solo se aplican los que vienen en la URL
public record FiltroEventosRequest(
        @Size(max = 80, message = "El nombre no puede superar los 80 caracteres")
        String nombre,

        @Positive(message = "El videojuego no es válido")
        Long videojuegoId,

        @Positive(message = "La categoría no es válida")
        Long categoriaId,

        @Size(max = 60, message = "El país no puede superar los 60 caracteres")
        String pais,

        @Size(max = 60, message = "La ciudad no puede superar los 60 caracteres")
        String ciudad,

        Modalidad modalidad,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate desde,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate hasta) {
}
