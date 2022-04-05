package ru.abondin.hreasy.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Hack to support both Flyway JDBC and Spring R2DBC urls to the database
 */
@ConfigurationProperties(prefix = "hreasy.db")
@Data
public class HrEasyDbProperties {
    private String host = "sql.hr";
    private int port = 1433;
    private String database = "master";
    private String username = "sa";
    private String password = "HREasyPassword2019!";
    /**
     * migrate, clean, repair, undo
     */
    private List<String> flywayCommands = new ArrayList<>();
}
