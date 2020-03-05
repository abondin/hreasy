package ru.abondin.hreasy.platform.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Hack to support both Flyway JDBC and Spring R2DBC urls to the database
 */
@ConfigurationProperties(prefix = "hreasy.db")
class HrEasyDbProperties {
    var host = "sql.hr";
    var port = 1433;
    var database = "master";
    var username = "sa";
    var password = "HREasyPassword2019!";
    /**
     * migrate, clean, repair, undo
     */
    var flywayCommands: Array<String> = arrayOf();
}