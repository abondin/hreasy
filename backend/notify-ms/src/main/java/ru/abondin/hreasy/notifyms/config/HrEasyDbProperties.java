package ru.abondin.hreasy.notifyms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Database properties shared by JDBC Flyway migrations and R2DBC repositories.
 */
@ConfigurationProperties(prefix = "hreasy.db")
@Data
public class HrEasyDbProperties {
    private String host;
    private int port = 5432;
    private String database = "master";
    private String username;
    private String password;
    /**
     * Flyway commands executed on application startup, for example migrate or repair.
     */
    private List<String> flywayCommands = new ArrayList<>();
}
