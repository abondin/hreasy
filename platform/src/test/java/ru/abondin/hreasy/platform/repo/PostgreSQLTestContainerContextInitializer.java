package ru.abondin.hreasy.platform.repo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Override r2dbc and flyway connection url to started testcontainers docker container.
 * Set hreasy.test.existing-database-docker=true to use your own docker container. Please be sure that hreasy.db properties are valid
 */
@Slf4j
public class PostgreSQLTestContainerContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    /**
     * Single container shared between test methods
     */
    private final static PostgreSQLContainer dbContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("hr-unit")
            .withUsername("hr");
    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        var existingSqlDocker = configurableApplicationContext
                .getEnvironment()
                .getProperty("hreasy.test.existing-database-docker", Boolean.class);
        if (existingSqlDocker != null && existingSqlDocker) {
            log.info("Use existing docker container for SQL Server");
        } else {
            log.info("Starting PostgreSQL Docker Container. If you want to use existing one, please set hreasy.test.existing-database-docker to true");
            dbContainer.start();
            var port = dbContainer.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT);
            TestPropertyValues.of(
                    "hreasy.db.host=" + dbContainer.getContainerIpAddress(),
                    "hreasy.db.port=" + port,
                    "hreasy.db.username=" + dbContainer.getUsername(),
                    "hreasy.db.password=" + dbContainer.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

}

