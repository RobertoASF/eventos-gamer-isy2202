package cl.duoc.gamer.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Size(max = 30, message = "El nombre de usuario no puede superar los 30 caracteres")
        String nombreUsuario,

        @NotBlank(message = "La clave es obligatoria")
        @Size(max = 72, message = "La clave no puede superar los 72 caracteres")
        String password) {
}
