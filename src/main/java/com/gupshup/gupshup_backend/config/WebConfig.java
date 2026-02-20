package com.gupshup.gupshup_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Agar koi /uploads/ wali link khole, to use folder me se file dikha do
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}