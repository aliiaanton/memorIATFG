package com.memoria.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@ConditionalOnProperty(name = "app.store", havingValue = "supabase")
public class SupabaseJdbcConfig {

    @Bean
    public DataSource supabaseDataSource(
            @Value("${app.supabase.db-url}") String dbUrl,
            @Value("${app.supabase.db-user}") String dbUser,
            @Value("${app.supabase.db-password}") String dbPassword) {
        JdbcConnectionSettings settings = toJdbcSettings(dbUrl, dbUser, dbPassword);
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(settings.jdbcUrl());
        dataSource.setUsername(settings.username());
        dataSource.setPassword(settings.password());
        return dataSource;
    }

    @Bean
    public JdbcTemplate supabaseJdbcTemplate(DataSource supabaseDataSource) {
        return new JdbcTemplate(supabaseDataSource);
    }

    private JdbcConnectionSettings toJdbcSettings(String dbUrl, String dbUser, String dbPassword) {
        if (dbUrl == null || dbUrl.isBlank()) {
            throw new IllegalStateException("SUPABASE_DB_URL is required when APP_STORE=supabase");
        }

        String normalizedUrl = dbUrl.trim();
        String parsedUser = null;
        String parsedPassword = null;

        if (normalizedUrl.startsWith("jdbc:postgresql://")) {
            String uriLikeUrl = "postgresql://" + normalizedUrl.substring("jdbc:postgresql://".length());
            ParsedPostgresUrl parsed = parsePostgresUrl(uriLikeUrl);
            normalizedUrl = "jdbc:" + parsed.urlWithoutCredentials();
            parsedUser = parsed.username();
            parsedPassword = parsed.password();
        } else if (normalizedUrl.startsWith("postgresql://")) {
            ParsedPostgresUrl parsed = parsePostgresUrl(normalizedUrl);
            normalizedUrl = "jdbc:" + parsed.urlWithoutCredentials();
            parsedUser = parsed.username();
            parsedPassword = parsed.password();
        } else if (!normalizedUrl.startsWith("jdbc:")) {
            normalizedUrl = "jdbc:" + normalizedUrl;
        }

        String username = hasText(parsedUser) ? parsedUser : dbUser;
        String password = hasText(parsedPassword) ? parsedPassword : dbPassword;

        if (!hasText(username)) {
            throw new IllegalStateException("SUPABASE_DB_USER is required when the database URL has no username");
        }
        if (!hasText(password)) {
            throw new IllegalStateException("SUPABASE_DB_PASSWORD is required when the database URL has no password");
        }

        return new JdbcConnectionSettings(ensureSslMode(normalizedUrl), username, password);
    }

    private ParsedPostgresUrl parsePostgresUrl(String postgresUrl) {
        URI uri = URI.create(postgresUrl);
        String userInfo = uri.getUserInfo();
        String username = null;
        String password = null;

        if (userInfo != null && !userInfo.isBlank()) {
            String[] parts = userInfo.split(":", 2);
            username = decode(parts[0]);
            if (parts.length > 1) {
                password = decode(parts[1]);
            }
        }

        StringBuilder cleanUrl = new StringBuilder("postgresql://")
                .append(uri.getHost());
        if (uri.getPort() > 0) {
            cleanUrl.append(":").append(uri.getPort());
        }
        cleanUrl.append(uri.getRawPath() == null || uri.getRawPath().isBlank() ? "/postgres" : uri.getRawPath());
        if (uri.getRawQuery() != null && !uri.getRawQuery().isBlank()) {
            cleanUrl.append("?").append(uri.getRawQuery());
        }

        return new ParsedPostgresUrl(cleanUrl.toString(), username, password);
    }

    private String ensureSslMode(String jdbcUrl) {
        if (jdbcUrl.contains("sslmode=")) {
            return jdbcUrl;
        }
        return jdbcUrl + (jdbcUrl.contains("?") ? "&" : "?") + "sslmode=require";
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record JdbcConnectionSettings(String jdbcUrl, String username, String password) {
    }

    private record ParsedPostgresUrl(String urlWithoutCredentials, String username, String password) {
    }
}
