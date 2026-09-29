package com.nexcare.backend.config;

import com.nexcare.backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter
            jwtAuthenticationFilter;

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Value(
            "${app.cors.allowed-origins:http://localhost:5173}"
    )
    private String allowedOrigins;

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        userDetailsService
                );

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .cors(Customizer.withDefaults())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        /*
                         * Authentication endpoints
                         */
                        .requestMatchers(
                                "/auth/signup",
                                "/auth/login"
                        )
                        .permitAll()

                        /*
                         * Doctor self-management must be declared before
                         * public doctor routes because /me could otherwise
                         * match a public wildcard.
                         */
                        .requestMatchers(
                                "/api/v1/doctors/me/**"
                        )
                        .hasRole("DOCTOR")

                        /*
                         * Authenticated doctor availability management must
                         * be matched before the public discovery patterns.
                         */
                        .requestMatchers(
                                "/api/v1/doctors/me/**"
                        )
                        .hasRole("DOCTOR")

                        /*
                         * Public doctor discovery.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/doctors",
                                "/api/v1/doctors/*",
                                "/api/v1/doctors/*/availability",
                                "/api/v1/doctors/*/reviews"
                        )
                        .permitAll()

                        /*
                         * Doctor profile management.
                         */
                        .requestMatchers("/doctor/**")
                        .hasRole("DOCTOR")

                        /*
                         * Patient profile management.
                         */
                        .requestMatchers("/patient/**")
                        .hasRole("PATIENT")

                        /*
                         * Patient appointment operations.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/appointments/book",
                                "/api/v1/appointments/*/review"
                        )
                        .hasRole("PATIENT")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/appointments/me"
                        )
                        .hasRole("PATIENT")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/appointments/*/cancel"
                        )
                        .hasRole("PATIENT")

                        /*
                         * Doctor appointment operations.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/appointments/doctor",
                                "/api/v1/appointments/doctor/summary"
                        )
                        .hasRole("DOCTOR")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/appointments/*/complete",
                                "/api/v1/appointments/*/no-show"
                        )
                        .hasRole("DOCTOR")

                        .anyRequest()
                        .authenticated()
                )

                .authenticationProvider(
                        authenticationProvider()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource
    corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        List<String> origins =
                Arrays.stream(
                                allowedOrigins.split(",")
                        )
                        .map(String::trim)
                        .filter(origin ->
                                !origin.isBlank()
                        )
                        .toList();

        configuration.setAllowedOrigins(origins);

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept"
                )
        );

        configuration.setExposedHeaders(
                List.of("Location")
        );

        configuration.setAllowCredentials(true);

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}
