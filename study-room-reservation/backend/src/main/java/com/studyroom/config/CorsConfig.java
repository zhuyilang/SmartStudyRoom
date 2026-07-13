package com.studyroom.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");              // 允许所有来源（开发环境）
        config.addAllowedHeader("*");                     // 允许所有请求头
        config.addAllowedMethod("*");                     // 允许所有请求方法
        config.setAllowCredentials(true);                 // 允许携带Cookie（Session需要）
        config.setMaxAge(3600L);                          // 预检请求缓存1小时

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
