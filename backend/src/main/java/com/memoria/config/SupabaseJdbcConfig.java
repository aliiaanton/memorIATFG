package com.memoria.config;

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
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(toJdbcUrl(dbUrl));
        dataSource.setUsername(dbUser);
        dataSource.setPassword(dbPassword);
        return dataSource;
    }

    @Bean
    public JdbcTemplate supabaseJdbcTemplate(DataSource supabaseDataSource) {
        return new JdbcTemplate(supabaseDataSource);
    }

    private String toJdbcUrl(String dbUrl) {
        if (dbUrl == null || dbUrl.isBlank()) {
            throw new IllegalStateException("SUPABASE_DB_URL is required when APP_STORE=supabase");
        }
        if (dbUrl.startsWith("jdbc:")) {
            return dbUrl;
        }
        if (dbUrl.startsWith("postgresql://")) {
            return "jdbc:" + dbUrl;
        }
        return dbUrl;
    }
}

