package org.example.project1.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * 注册拦截器
 *
 * @Author djy
 * @Date 2026/01/08
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private UserContextInterceptor userContextInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/**")  // 拦截所有路径
                .excludePathPatterns(
                        "/auth/**",      // 排除认证相关接口（登录、注册等）
                        "/error",        // 排除错误页面
                        "/favicon.ico"   // 排除图标
                );
    }
}


