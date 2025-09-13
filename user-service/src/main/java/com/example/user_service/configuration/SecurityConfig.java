package com.example.user_service.configuration;

import com.example.user_service.enums.UserRole;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private CustomJwtDecoder customJwtDecoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/users/register",
                                "/api/users/email/{userId}",
                                "/api/users/update/{id}",
                                "/api/users/login",
                                "/api/users/refresh",
                                "/api/users/logout",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/users/getAllUser",
                                "/api/users/getUser/{id}"
                        ).permitAll()
                        .requestMatchers("/api/admin/**").hasRole(UserRole.ADMIN.name())
                        // Cho DELETE có thể yêu cầu auth hoặc permitAll tuỳ nhu cầu
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(bearerTokenResolver()) // thêm dòng này
                        .jwt(jwtConfigurer ->
                                jwtConfigurer
                                        .decoder(customJwtDecoder)
                                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthorityPrefix(""); 
        converter.setAuthoritiesClaimName("scope");

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);
        return jwtConverter;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // ✅ Thêm BearerTokenResolver để bỏ qua JWT filter cho DELETE /api/users/**
    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        DefaultBearerTokenResolver base = new DefaultBearerTokenResolver();
        return request -> {
            String uri = request.getRequestURI();
            String method = request.getMethod();
            if ("DELETE".equals(method) && uri.contains("/api/users/")) {
                return null; // coi như không có token → không 401
            }
            return base.resolve(request);
        };
    }
}
