package com.interviewprep.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DataSourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username:root}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    @Value("${spring.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String driverClassName;

    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.profiles.active", havingValue = "mysql", matchIfMissing = true)
    public DataSource dataSource() {
        String resolvedUrl = url;
        String resolvedUser = username;
        String resolvedPass = password;

        // Auto-convert standard cloud mysql:// URLs (e.g. Railway, Clever Cloud, Aiven, Render) to jdbc:mysql://
        if (resolvedUrl != null && resolvedUrl.startsWith("mysql://")) {
            try {
                URI uri = new URI(resolvedUrl.replace("mysql://", "http://"));
                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 3306 : uri.getPort();
                String path = uri.getPath(); // /dbname
                String query = uri.getQuery();

                if (uri.getUserInfo() != null) {
                    String[] userInfo = uri.getUserInfo().split(":", 2);
                    resolvedUser = userInfo[0];
                    if (userInfo.length > 1) {
                        resolvedPass = userInfo[1];
                    }
                }

                if (query != null) {
                    query = query.replace("ssl-mode=", "sslMode=");
                }

                resolvedUrl = "jdbc:mysql://" + host + ":" + port + path + (query != null ? "?" + query : "");
                logger.info("Converted cloud mysql:// URL to JDBC format: {}", resolvedUrl.replaceAll(":[^/@]+@", ":****@"));
            } catch (Exception e) {
                logger.warn("Could not parse cloud mysql:// URL as URI, prepending jdbc:: {}", e.getMessage());
                resolvedUrl = "jdbc:" + resolvedUrl;
            }
        }

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(resolvedUrl);
        ds.setUsername(resolvedUser);
        ds.setPassword(resolvedPass);
        ds.setDriverClassName(driverClassName);
        ds.setMaximumPoolSize(10);
        ds.setMinimumIdle(2);
        ds.setConnectionTimeout(30000);
        ds.setIdleTimeout(600000);
        ds.setMaxLifetime(1800000);
        ds.setKeepaliveTime(30000);
        ds.setValidationTimeout(5000);

        Exception lastException = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try (java.sql.Connection conn = ds.getConnection()) {
                logger.info("Connected to MySQL DataSource with URL: {}", resolvedUrl != null ? resolvedUrl.replaceAll(":[^/@]+@", ":****@") : "null");
                return ds;
            } catch (Exception e) {
                lastException = e;
                logger.warn("MySQL connection attempt {}/3 failed: {}. Retrying in 2 seconds...", attempt, e.getMessage());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
            }
        }

        if (resolvedUrl != null && !resolvedUrl.contains("localhost") && !resolvedUrl.contains("127.0.0.1")) {
            logger.error("FATAL: Could not connect to remote Cloud MySQL database at {}. Aborting startup to guarantee data is stored permanently in Cloud MySQL and prevent data loss.",
                    resolvedUrl.replaceAll(":[^/@]+@", ":****@"));
            throw new IllegalStateException("Failed to connect to Cloud MySQL database at " + resolvedUrl.replaceAll(":[^/@]+@", ":****@") + ". Refusing fallback to protect cloud data integrity.", lastException);
        }

        logger.warn("Could not connect to local MySQL after 3 attempts at {}. Activating zero-config embedded database fallback for local offline development. Error: {}",
                resolvedUrl != null ? resolvedUrl.replaceAll(":[^/@]+@", ":****@") : "null",
                lastException != null ? lastException.getMessage() : "Unknown");
        try {
            ds.close();
        } catch (Exception ignored) {}

        HikariDataSource fallback = new HikariDataSource();
        fallback.setJdbcUrl("jdbc:h2:file:./data/interviewprep_db;DB_CLOSE_DELAY=-1");
        fallback.setDriverClassName("org.h2.Driver");
        fallback.setUsername("sa");
        fallback.setPassword("");
        fallback.setMaximumPoolSize(10);
        fallback.setMinimumIdle(2);
        fallback.setConnectionTimeout(30000);
        logger.info("Fallback persistent H2 database activated successfully for local dev.");
        return fallback;
    }
}
