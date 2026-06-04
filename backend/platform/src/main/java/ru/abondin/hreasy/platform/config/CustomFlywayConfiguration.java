package ru.abondin.hreasy.platform.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import ru.abondin.hreasy.backend.common.flyway.FlywayMigrationSupport;

/**
 * Runs Flyway migrations for the R2DBC-based platform service.
 */
@Configuration
@Slf4j
public class CustomFlywayConfiguration {

    /**
     * Exposes HR Easy database properties used by the migration strategy.
     *
     * @return database properties bound from {@code hreasy.db.*}
     */
    @Bean
    HrEasyDbProperties hrEasyDbProperties() {
        return new HrEasyDbProperties();
    }

    /**
     * Creates Flyway explicitly because Spring Flyway auto-configuration does not run with R2DBC only.
     *
     * @param environment Spring environment with standard {@code spring.flyway.*} properties
     * @param hrEasyDbProperties HR Easy database properties with migration commands
     * @return configured Flyway instance after startup commands are executed
     */
    @Bean
    @ConditionalOnProperty("spring.flyway.url")
    Flyway flyway(Environment environment, HrEasyDbProperties hrEasyDbProperties) {
        var url = environment.getProperty("spring.flyway.url");
        var user = environment.getProperty("spring.flyway.user");
        log.info("Initialize Flyway Migration url={}, user={}",
                url,
                user);
        return FlywayMigrationSupport.createAndRun(environment, hrEasyDbProperties.getFlywayCommands(), log::warn);
    }
}
