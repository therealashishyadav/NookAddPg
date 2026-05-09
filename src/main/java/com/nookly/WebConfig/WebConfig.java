package com.nookly.WebConfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// ── FILE LOCATION: src/main/java/com/nookly/config/WebConfig.java ─────────
// This file does TWO things:
// 1. Fixes CORS — allows Angular (localhost:4200) to call Spring Boot (localhost:8084)
// 2. Serves /uploads/ folder as public static files so Angular can load images
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // ── CORS — allows Angular to talk to Spring Boot ──────────────────────
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")               // apply to all endpoints
                .allowedOrigins(
                    "http://localhost:4200",      // Angular dev server
                    "http://localhost:3000"       // in case you use another port
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    // ── Static files — serves images from /uploads/ folder ───────────────
    // e.g. http://localhost:8084/uploads/abc123_room.jpg
    //   → serves file from uploads/abc123_room.jpg on your laptop
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}