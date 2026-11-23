package cl.duoc.gamer.web.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;
import org.springframework.security.web.header.writers.CrossOriginEmbedderPolicyHeaderWriter.CrossOriginEmbedderPolicy;
import org.springframework.security.web.header.writers.CrossOriginOpenerPolicyHeaderWriter.CrossOriginOpenerPolicy;
import org.springframework.security.web.header.writers.CrossOriginResourcePolicyHeaderWriter.CrossOriginResourcePolicy;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

@Configuration
@EnableWebSecurity
public class SeguridadConfig {

    // El sitio no usa scripts, estilos ni imagenes de otros dominios, asi que todo se limita al propio origen
    private static final String POLITICA_CONTENIDO = "default-src 'self'; script-src 'self'; style-src 'self'; "
            + "img-src 'self' data:; font-src 'self'; connect-src 'self'; object-src 'none'; base-uri 'self'; "
            + "form-action 'self'; frame-ancestors 'none'";

    private static final String POLITICA_PERMISOS = "camera=(), microphone=(), geolocation=(), payment=(), usb=()";

    // ProveedorAutenticacionApi es el unico AuthenticationProvider del contexto, asi que Spring Security
    // lo registra solo. Si ademas se agrega aqui, un login fallido se intenta dos veces (cadena local y global)
    @Bean
    public SecurityFilterChain filtroSeguridad(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(rutas -> rutas
                        // Paginas publicas del caso: inicio y busqueda. El detalle de un evento, las fotos y videos,
                        // las opiniones, compartir y la pagina Comunidad piden sesion (anyRequest)
                        .requestMatchers("/", "/inicio", "/buscar", "/login", "/error").permitAll()
                        .requestMatchers("/css/**", "/img/**", "/js/**", "/favicon.svg").permitAll()
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/", false)
                        .failureHandler(manejadorLoginFallido())
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?salir")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .sessionManagement(sesion -> sesion
                        // Al iniciar sesion se cambia el id de la sesion para evitar la fijacion de sesion
                        .sessionFixation(fijacion -> fijacion.changeSessionId()))
                .headers(cabeceras -> cabeceras
                        .contentSecurityPolicy(csp -> csp.policyDirectives(POLITICA_CONTENIDO))
                        .referrerPolicy(referencia -> referencia.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .permissionsPolicyHeader(permisos -> permisos.policy(POLITICA_PERMISOS))
                        .crossOriginOpenerPolicy(coop -> coop.policy(CrossOriginOpenerPolicy.SAME_ORIGIN))
                        .crossOriginEmbedderPolicy(coep -> coep.policy(CrossOriginEmbedderPolicy.REQUIRE_CORP))
                        .crossOriginResourcePolicy(corp -> corp.policy(CrossOriginResourcePolicy.SAME_ORIGIN)));
        return http.build();
    }

    // Cada motivo de rechazo muestra su propio mensaje, sin revelar si el usuario existe
    private ExceptionMappingAuthenticationFailureHandler manejadorLoginFallido() {
        ExceptionMappingAuthenticationFailureHandler manejador = new ExceptionMappingAuthenticationFailureHandler();
        manejador.setDefaultFailureUrl("/login?error");
        manejador.setExceptionMappings(Map.of(
                LockedException.class.getName(), "/login?bloqueado",
                AuthenticationServiceException.class.getName(), "/login?servicio"));
        return manejador;
    }
}
