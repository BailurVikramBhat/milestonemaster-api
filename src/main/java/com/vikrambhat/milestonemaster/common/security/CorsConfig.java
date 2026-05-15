package com.vikrambhat.milestonemaster.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {
    @Bean
    CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration con = new CorsConfiguration();
        con.setAllowedOrigins(corsProperties.allowedOrigins());
        con.setAllowedMethods(corsProperties.allowedMethods());
        con.setAllowedHeaders(corsProperties.allowedHeaders());
        con.setAllowCredentials(corsProperties.allowCredentials());
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", con);
        return src;
    }
}
