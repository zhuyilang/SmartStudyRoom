package com.studyroom.config;

import com.studyroom.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")                                    // 拦截所有
                .excludePathPatterns(
                        "/api/auth/login",                                // 登录
                        "/api/auth/register",                             // 注册
                        "/doc.html",                                      // Knife4j 文档页
                        "/swagger-ui/**",                                 // Swagger 资源
                        "/v3/api-docs/**",                                // OpenAPI 文档
                        "/webjars/**",                                    // 静态资源
                        "/error"                                          // 错误页面
                );
    }
}
