package com.vikrambhat.milestonemaster.common.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {
    private static final Logger log = LoggerFactory.getLogger(CorsConfig.class);

    @Bean
    CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration con = new CorsConfiguration();
        con.setAllowedOrigins(corsProperties.allowedOrigins());
        con.setAllowedMethods(corsProperties.allowedMethods());
        con.setAllowedHeaders(corsProperties.allowedHeaders());
        con.setAllowCredentials(corsProperties.allowCredentials());
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", con);
        log.info("Configured CORS for origins={}", corsProperties.allowedOrigins());
        return src;
    }
}
