package ru.abondin.hreasy.platform.repo

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.MSSQLServerContainer
import ru.abondin.hreasy.platform.logger


/**
 * Override r2dbc and flyway connection url to started testcontainers docker container.
 * Set hreasy.test.existing-database-docker=true to use your own docker container. Please be sure that hreasy.db properties are valid
 */
class SqlServerContextInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
        val existingSqlDocker: Boolean = configurableApplicationContext.environment.getProperty("hreasy.test.existing-database-docker", Boolean::class.java)
                ?: false;
        if (existingSqlDocker) {
            logger().info("Use existing docker container for SQL Server");
        } else {
            logger().info("Starting SQL Server Docker Container. If you want to use existing one, please set hreasy.test.existing-database-docker to true");
            sqlServer.start();
            val port = sqlServer.getMappedPort(MSSQLServerContainer.MS_SQL_SERVER_PORT)
            TestPropertyValues.of(
                    "hreasy.db.host=${sqlServer.containerIpAddress}",
                    "hreasy.db.port=${port}",
                    "hreasy.db.username=" + sqlServer.getUsername(),
                    "hreasy.db.password=" + sqlServer.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    companion object {
        val sqlServer = KMSSQLServerContainer()
                .withEnv("MSSQL_COLLATION", "Cyrillic_General_100_CI_AS");
    }
}

/**
 * Hack to work with GenericContainer<SELF>
 * https://github.com/testcontainers/testcontainers-java/issues/318
 */
class KMSSQLServerContainer() : MSSQLServerContainer<KMSSQLServerContainer>();