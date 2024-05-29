package it.unisalento.pasproject.authservice.configuration;

import it.unisalento.pasproject.authservice.security.ExceptionFilter;
import it.unisalento.pasproject.authservice.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import java.util.List;

@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

    private final List<String> ignoredUrls = List.of("/api/authenticate", "/api/registration", "/api/recover/**");

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //Filtri della chiamata
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
/*        http.authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/api/authenticate","/api/registration","/api/recover/{token}").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));*/

        // Configurazione CORS
        http.cors(AbstractHttpConfigurer::disable); // Disabilita CORS, se necessario

        // Configurazione CSRF
        http.csrf(AbstractHttpConfigurer::disable); // Disabilita CSRF

        // Configurazione gestione eccezioni, adatta la gestione eccezioni al Servlet (carica prima degli altri componenti)
        http.addFilterBefore(exceptionFilter(), LogoutFilter.class);

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(ignoredUrls);
    }

    @Bean
    public ExceptionFilter exceptionFilter() {
        return new ExceptionFilter();
    }
}
