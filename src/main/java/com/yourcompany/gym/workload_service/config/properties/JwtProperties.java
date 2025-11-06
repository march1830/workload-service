package com.yourcompany.gym.workload_service.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

//TODO: delete or use
@Data
@Configuration
@ConfigurationProperties(prefix = "application.security.jwt")
public class JwtProperties {
    private String secretKey;
    private long jwtExpiration;
}
