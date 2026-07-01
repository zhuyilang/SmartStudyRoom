package com.studyroom.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("自习室预约系统接口文档")
                        .version("1.0.0")
                        .description("智能校园自习室预约管理平台 API")
                        .contact(new Contact().name("项目组").email("team@studyroom.com")));
    }
}
