package cl.duoc.gamer.eventos.security;

import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

// Permite recibir un UsuarioToken como parametro de los controladores, armado con el token ya validado
@Component
public class UsuarioTokenResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parametro) {
        return UsuarioToken.class.equals(parametro.getParameterType());
    }

    @Override
    public UsuarioToken resolveArgument(MethodParameter parametro, ModelAndViewContainer contenedor,
            NativeWebRequest request, WebDataBinderFactory fabrica) {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !(autenticacion.getPrincipal() instanceof Jwt jwt)) {
            throw new AuthenticationCredentialsNotFoundException("La ruta necesita un token valido");
        }
        return UsuarioToken.desde(jwt);
    }
}
