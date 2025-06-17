package edu.hm.cs.kreisel_backend.config;

import edu.hm.cs.kreisel_backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/h2-console/**").permitAll()
                        // Öffentlichen Zugang zu Bildern erlauben
                        .requestMatchers("/api/items/images/**").permitAll()
                        // Öffentlichen Zugriff auf Item-Details erlauben
                        .requestMatchers(HttpMethod.GET, "/api/items", "/api/items/{id}").permitAll()
                        // Zugriff auf Reviews für alle erlauben (nur Lesen)
                        .requestMatchers(HttpMethod.GET, "/api/reviews/item/**").permitAll()

                        // Admin-only endpoints
                        .requestMatchers("/api/users").hasRole("ADMIN")
                        .requestMatchers("/api/users/{id}").hasRole("ADMIN")
                        .requestMatchers("/api/users/email/{email}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/{id}").hasRole("ADMIN")

                        // Bild-Upload nur für Admins erlauben
                        .requestMatchers(HttpMethod.POST, "/api/items/{id}/image").hasRole("ADMIN")

                        // Item-Verwaltung nur für Admins
                        .requestMatchers(HttpMethod.POST, "/api/items").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/items/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/items/{id}").hasRole("ADMIN")

                        // User kann seine eigenen Daten zugreifen
                        .requestMatchers("/api/users/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/users/me").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/users/me").authenticated()

                        // Review-Funktionalität für authentifizierte Benutzer
                        .requestMatchers(HttpMethod.POST, "/api/reviews").authenticated()
                        .requestMatchers("/api/reviews/can-review/**").authenticated()

                        // User kann auf seine eigenen Vermietungen zugreifen
                        .requestMatchers("/api/rentals/user/**").authenticated()
                        .requestMatchers("/api/rentals/rent").authenticated()
                        .requestMatchers("/api/rentals/{rentalId}/extend").authenticated()
                        .requestMatchers("/api/rentals/{rentalId}/return").authenticated()

                        // Admin kann auf alle Vermietungen zugreifen
                        .requestMatchers("/api/rentals").hasRole("ADMIN")
                        .requestMatchers("/api/rentals/user/{userId}/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .headers(headers -> headers
                        .frameOptions(FrameOptionsConfig::disable)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}