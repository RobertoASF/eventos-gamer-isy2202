package cl.duoc.gamer.web.modelo;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CompartirForm {

    @Size(max = 280, message = "El mensaje puede tener hasta 280 caracteres")
    private String mensaje;
}
