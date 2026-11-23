package cl.duoc.gamer.usuarios.config;

import cl.duoc.gamer.usuarios.security.RespuestasSeguridad;
import cl.duoc.gamer.usuarios.service.TokenService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@RequiredArgsConstructor
public class SeguridadConfig {

    private final RespuestasSeguridad respuestasSeguridad;

    @Bean
    public SecurityFilterChain filtroSeguridad(HttpSecurity http) throws Exception {
        http
                // La proteccion CSRF queda activa en todas las rutas, sin excepciones. Como la API no guarda sesion,
                // el token CSRF se valida con doble envio: viaja en una cookie y tambien en un encabezado, y deben
                // coincidir. El login lo pide antes en GET /api/auth/csrf. Las peticiones que ya traen el JWT en el
                // encabezado Authorization no lo necesitan: Spring Security las reconoce porque un sitio externo no
                // puede hacer que el navegador envie ese encabezado
                .csrf(csrf -> csrf
                        .csrfTokenRepository(repositorioCsrf())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rutas -> rutas
                        .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/actuator/health/**", "/error").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(servidor -> servidor
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(conversorJwt()))
                        .authenticationEntryPoint(respuestasSeguridad)
                        .accessDeniedHandler(respuestasSeguridad))
                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint(respuestasSeguridad)
                        .accessDeniedHandler(respuestasSeguridad));
        return http.build();
    }

    // Cookie del token CSRF: solo la lee el servidor (HttpOnly) y nunca se envia desde otros sitios
    private CookieCsrfTokenRepository repositorioCsrf() {
        CookieCsrfTokenRepository repositorio = new CookieCsrfTokenRepository();
        repositorio.setCookieCustomizer(cookie -> cookie.sameSite("Strict"));
        return repositorio;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecretKey claveJwt(@Value("${jwt.secreto}") String secreto) {
        return new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey claveJwt) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(claveJwt));
    }

    // Ademas de la firma y la fecha de expiracion, exige que el token lo haya emitido este servicio
    @Bean
    public JwtDecoder jwtDecoder(SecretKey claveJwt) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(claveJwt).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(TokenService.EMISOR));
        return decoder;
    }

    // El claim "rol" del token pasa a ser la autoridad ROLE_X
    private JwtAuthenticationConverter conversorJwt() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("rol");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(roles);
        return conversor;
    }
}
