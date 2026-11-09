package cl.duoc.gamer.web.config;

import cl.duoc.gamer.web.seguridad.ProveedorAutenticacionApi;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SeguridadConfig {

    private final ProveedorAutenticacionApi proveedorAutenticacion;

    @Bean
    public SecurityFilterChain filtroSeguridad(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(proveedorAutenticacion)
                .authorizeHttpRequests(rutas -> rutas
                        // Paginas publicas del caso: inicio y busqueda. El detalle de un evento pide sesion
                        .requestMatchers("/", "/inicio", "/buscar", "/login", "/error").permitAll()
                        .requestMatchers("/css/**", "/img/**", "/favicon.svg").permitAll()
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/", false)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?salir")
                        .permitAll());
        return http.build();
    }
}
