package ru.abondin.hreasy.platform.config

import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.boot.autoconfigure.flyway.FlywayProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.abondin.hreasy.platform.logger

/**
 * Spring Flyway does not work with r2dbc by default.
 * So we are using native Flyway API
 */
@Configuration
class CustomFlywayConfiguration() {

    @Bean
    fun flywayProperties(): FlywayProperties = FlywayProperties();

    @Bean
    fun hrEasyDbProperties(): HrEasyDbProperties = HrEasyDbProperties();

    @Bean
    @ConditionalOnProperty("spring.flyway.url")
    fun flywayMigrationInitializer(flywayProperties: FlywayProperties, hrEasyDbProperties: HrEasyDbProperties): FlywayMigrationInitializer {
        val flyway = Flyway.configure()
                .dataSource(flywayProperties.url, flywayProperties.user, flywayProperties.password)
                .baselineVersion(flywayProperties.baselineVersion)
                .baselineOnMigrate(flywayProperties.isBaselineOnMigrate)
                .load();
        return FlywayMigrationInitializer(flyway, CustomFlywayMigrationStrategy(hrEasyDbProperties.flywayCommands));

    }
}

class CustomFlywayMigrationStrategy(val commands: Array<String>) : FlywayMigrationStrategy {
    override fun migrate(flyway: Flyway) {
        if (commands.isEmpty()) {
            logger().warn("No Flyway migration commands passed. Allowed values: baseline, migrate, clean, repair, undo");
        }
        for (command in commands) {
            when (command) {
                "baseline" -> flyway.baseline();
                "migrate" -> flyway.migrate();
                "clean" -> flyway.clean();
                "repair" -> flyway.repair();
                "undo" -> flyway.undo();
                else -> {
                    throw IllegalArgumentException("Unsupported flyway command ${command}");
                }
            }
        }
    }

}
