package cl.duoc.gamer.web.modelo;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

// Datos del formulario de busqueda. Se validan aqui y otra vez en ms-eventos
@Getter
@Setter
public class FiltroBusqueda {

    @Size(max = 80, message = "El nombre puede tener hasta 80 caracteres")
    private String nombre;

    @Positive(message = "Elige un videojuego de la lista")
    private Long videojuegoId;

    @Positive(message = "Elige una categoría de la lista")
    private Long categoriaId;

    @Size(max = 60, message = "El país puede tener hasta 60 caracteres")
    private String pais;

    @Size(max = 60, message = "La ciudad puede tener hasta 60 caracteres")
    private String ciudad;

    @Pattern(regexp = "PRESENCIAL|ONLINE", message = "Elige una modalidad de la lista")
    private String modalidad;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate desde;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate hasta;
}
