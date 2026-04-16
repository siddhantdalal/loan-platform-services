package com.loanplatform.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Value("${services.user-service.url}")
    private String userServiceUrl;

    @Value("${services.loan-service.url}")
    private String loanServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-auth", r -> r
                        .path("/api/auth/**")
                        .uri(userServiceUrl))
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .uri(userServiceUrl))
                .route("loan-service", r -> r
                        .path("/api/loans/**")
                        .uri(loanServiceUrl))
                .build();
    }
}
