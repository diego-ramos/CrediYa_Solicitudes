package com.crediya.r2dbc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapters.mariadb")
public record MariaDbConnectionProperties(
        String host,
        Integer port,
        String database,
        String username,
        String password) {
}
