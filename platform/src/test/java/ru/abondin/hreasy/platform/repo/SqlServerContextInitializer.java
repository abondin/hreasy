package ru.abondin.hreasy.platform.repo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MSSQLServerContainer;

/**
 * Override r2dbc and flyway connection url to started testcontainers docker container.
 * Set hreasy.test.existing-database-docker=true to use your own docker container. Please be sure that hreasy.db properties are valid
 */
@Slf4j
public class SqlServerContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    /**
     * Single container shared between test methods
     */
    private final static MSSQLServerContainer sqlServer = new MSSQLServerContainer<>()
            .withEnv("MSSQL_COLLATION", "Cyrillic_General_100_CI_AS");

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        var existingSqlDocker = configurableApplicationContext
                .getEnvironment()
                .getProperty("hreasy.test.existing-database-docker", Boolean.class);
        if (existingSqlDocker != null && existingSqlDocker) {
            log.info("Use existing docker container for SQL Server");
        } else {
            log.info("Starting SQL Server Docker Container. If you want to use existing one, please set hreasy.test.existing-database-docker to true");
            sqlServer.start();
            var port = sqlServer.getMappedPort(MSSQLServerContainer.MS_SQL_SERVER_PORT);
            TestPropertyValues.of(
                    "hreasy.db.host=" + sqlServer.getContainerIpAddress(),
                    "hreasy.db.port=" + port,
                    "hreasy.db.username=" + sqlServer.getUsername(),
                    "hreasy.db.password=" + sqlServer.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

}

