package cl.duoc.gamer.eventos.service;

import java.util.regex.Pattern;

// Limpia el texto que escriben los usuarios antes de guardarlo. El escape para HTML lo hace Thymeleaf
// al mostrarlo; aqui solo se sacan los caracteres de control, que no tienen uso en un comentario
// y sirven para falsificar lineas en los registros o esconder contenido
final class TextoSeguro {

    private static final Pattern CONTROL_SIN_SALTOS = Pattern.compile("[\\p{Cntrl}&&[^\\n]]");
    private static final Pattern CONTROL = Pattern.compile("\\p{Cntrl}");
    private static final Pattern SALTOS_REPETIDOS = Pattern.compile("\\n{3,}");

    private TextoSeguro() {
    }

    // Texto de una sola linea, por ejemplo la descripcion de una foto
    static String linea(String texto) {
        if (texto == null) {
            return "";
        }
        return CONTROL.matcher(texto).replaceAll(" ").strip();
    }

    // Texto que puede tener varios parrafos, como un comentario
    static String parrafos(String texto) {
        if (texto == null) {
            return "";
        }
        String normalizado = texto.replace("\r\n", "\n").replace('\r', '\n');
        String limpio = CONTROL_SIN_SALTOS.matcher(normalizado).replaceAll(" ");
        return SALTOS_REPETIDOS.matcher(limpio).replaceAll("\n\n").strip();
    }
}
