package org.example.project1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置类
 * 用于处理前端跨域请求
 *
 * @Author djy
 * @Date 2026/01/06
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 允许的跨域源（从配置文件读取，多个用逗号分隔）
     * 如果为空，则允许所有源（开发环境）
     */
    @Value("${cors.allowed-origins:}")
    private String allowedOrigins;

    /**
     * 是否允许携带凭证（Cookie等）
     */
    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;

    /**
     * 预检请求的缓存时间（秒）
     */
    @Value("${cors.max-age:3600}")
    private long maxAge;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        CorsRegistration registration = registry.addMapping("/**");  // 对所有路径生效
        
        // 如果配置了具体的源，使用 allowedOrigins（支持携带凭证）
        if (allowedOrigins != null && !allowedOrigins.trim().isEmpty()) {
            String[] origins = allowedOrigins.split(",");
            // 去除空格
            for (int i = 0; i < origins.length; i++) {
                origins[i] = origins[i].trim();
            }
            registration.allowedOrigins(origins);
            // 配置了具体源时，可以允许携带凭证
            if (allowCredentials) {
                registration.allowCredentials(true);
            }
        } else {
            // 如果没有配置，使用 allowedOriginPatterns 允许所有源（开发环境）
            // 注意：当使用 "*" 时，不能同时设置 allowCredentials 为 true
            registration.allowedOriginPatterns("*");
            registration.allowCredentials(false);  // 允许所有源时，不能携带凭证
        }
        
        registration
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")  // 允许的HTTP方法
                .allowedHeaders("*")  // 允许所有请求头
                .exposedHeaders("*")  // 暴露所有响应头
                .maxAge(maxAge);  // 预检请求缓存时间
    }
}

