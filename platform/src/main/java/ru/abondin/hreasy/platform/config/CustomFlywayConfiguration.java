package ru.abondin.hreasy.platform.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Spring Flyway does not work with r2dbc by default.
 * So we are using native Flyway API
 */
@Configuration
@Slf4j
public class CustomFlywayConfiguration {
    @Bean
    FlywayProperties flywayProperties() {
        return new FlywayProperties();
    }

    @Bean
    HrEasyDbProperties hrEasyDbProperties() {
        return new HrEasyDbProperties();
    }

    @Bean
    @ConditionalOnProperty("spring.flyway.url")
    FlywayMigrationInitializer flywayMigrationInitializer(FlywayProperties flywayProperties,
                                                          HrEasyDbProperties hrEasyDbProperties) {
        log.info("Initialize Flyway Migration url={}, user={}",
                flywayProperties.getUrl(),
                flywayProperties.getUser());
        var flyway = Flyway.configure()
                .dataSource(flywayProperties.getUrl(),
                        flywayProperties.getUser(),
                        flywayProperties.getPassword())
                .baselineVersion(flywayProperties.getBaselineVersion())
                .baselineOnMigrate(flywayProperties.isBaselineOnMigrate())
                .locations(flywayProperties.getLocations().toArray(new String[0]))
                .load();
        return new FlywayMigrationInitializer(flyway,
                new CustomFlywayMigrationStrategy(hrEasyDbProperties.getFlywayCommands()));

    }


    @RequiredArgsConstructor
    class CustomFlywayMigrationStrategy implements FlywayMigrationStrategy {
        private final List<String> commands;

        @Override
        public void migrate(Flyway flyway) {
            if (commands.isEmpty()) {
                log.warn("No Flyway migration commands passed. Allowed values: baseline, migrate, clean, repair, undo");
            }
            for (var command : commands) {
                switch (command) {
                    case "baseline":
                        flyway.baseline();
                        break;
                    case "migrate":
                        flyway.migrate();
                        break;
                    case "clean":
                        flyway.clean();
                        break;
                    case "repair":
                        flyway.repair();
                        break;
                    case "undo":
                        flyway.undo();
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported flyway command " + command);
                }
            }
        }

    }
}
