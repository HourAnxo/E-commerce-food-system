package com.example.E_commerce_food_system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Allows the configured frontend origin(s) to call the REST API.
 * Defaults to the Vite dev server (http://localhost:5173); override in
 * production via the app.cors.allowed-origins property / CORS_ALLOWED_ORIGINS
 * env var (comma-separated). Not needed when Nginx serves the frontend and
 * proxies /api on the same origin.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
