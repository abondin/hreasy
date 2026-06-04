package ru.abondin.hreasy.backend.common.flyway;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.function.Consumer;

/**
 * Creates and runs Flyway migrations for R2DBC services that still need JDBC-based Flyway startup.
 */
public final class FlywayMigrationSupport {

    private FlywayMigrationSupport() {
    }

    /**
     * Creates Flyway from standard {@code spring.flyway.*} properties and executes configured commands.
     *
     * @param environment Spring environment with Flyway properties
     * @param commands commands to execute after Flyway is configured
     * @param emptyCommandsHandler callback invoked when command list is empty
     * @return configured Flyway instance after startup commands are executed
     */
    public static Flyway createAndRun(Environment environment,
                                      List<String> commands,
                                      Consumer<String> emptyCommandsHandler) {
        var configuration = Flyway.configure()
                .dataSource(required(environment, "spring.flyway.url"),
                        environment.getProperty("spring.flyway.user"),
                        environment.getProperty("spring.flyway.password"))
                .baselineVersion(MigrationVersion.fromVersion(environment.getProperty("spring.flyway.baseline-version", "1")))
                .baselineOnMigrate(environment.getProperty("spring.flyway.baseline-on-migrate", Boolean.class, false))
                .locations(list(environment, "spring.flyway.locations", "classpath:db/migration"));

        var schemas = list(environment, "spring.flyway.schemas", "");
        if (schemas.length > 0) {
            configuration.schemas(schemas);
        }

        var flyway = configuration.load();
        FlywayCommands.run(flyway, commands, emptyCommandsHandler);
        return flyway;
    }

    private static String required(Environment environment, String key) {
        var value = environment.getProperty(key);
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException("Required property is empty: " + key);
        }
        return value;
    }

    private static String[] split(String value) {
        if (!StringUtils.hasText(value)) {
            return new String[0];
        }
        return StringUtils.commaDelimitedListToStringArray(value);
    }

    private static String[] list(Environment environment, String key, String defaultValue) {
        return Binder.get(environment)
                .bind(key, String[].class)
                .orElseGet(() -> split(environment.getProperty(key, defaultValue)));
    }
}
