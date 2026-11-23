package cl.duoc.gamer.eventos.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ComentarioRequest(
        @NotNull(message = "Debes elegir una puntuación")
        @Min(value = 1, message = "La puntuación mínima es 1")
        @Max(value = 5, message = "La puntuación máxima es 5")
        Integer puntuacion,

        @NotBlank(message = "El comentario no puede quedar vacío")
        @Size(max = 500, message = "El comentario puede tener hasta 500 caracteres")
        String texto) {
}
