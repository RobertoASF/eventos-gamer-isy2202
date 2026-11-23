package cl.duoc.gamer.web.modelo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Formulario de comentario y valoracion. La API vuelve a validar lo mismo, esto es para avisar antes
@Getter
@Setter
@NoArgsConstructor
public class OpinionForm {

    @NotNull(message = "Elige de 1 a 5 estrellas")
    @Min(value = 1, message = "Elige de 1 a 5 estrellas")
    @Max(value = 5, message = "Elige de 1 a 5 estrellas")
    private Integer puntuacion;

    @NotBlank(message = "Escribe un comentario")
    @Size(max = 500, message = "El comentario puede tener hasta 500 caracteres")
    private String texto;

    public OpinionForm(Integer puntuacion, String texto) {
        this.puntuacion = puntuacion;
        this.texto = texto;
    }
}
