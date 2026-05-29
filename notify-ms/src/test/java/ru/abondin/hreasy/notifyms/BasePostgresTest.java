package ru.abondin.hreasy.notifyms;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public abstract class BasePostgresTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("hreasy_notify_test")
            .withUsername("hreasy")
            .withPassword("hreasy");

    @BeforeAll
    static void migrateDatabase() {
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .schemas("notify_ms")
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://"
                + POSTGRES.getHost() + ":" + POSTGRES.getMappedPort(5432) + "/" + POSTGRES.getDatabaseName()
                + "?schema=notify_ms");
        registry.add("spring.r2dbc.username", POSTGRES::getUsername);
        registry.add("spring.r2dbc.password", POSTGRES::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.url", POSTGRES::getJdbcUrl);
        registry.add("spring.flyway.user", POSTGRES::getUsername);
        registry.add("spring.flyway.password", POSTGRES::getPassword);
        registry.add("hreasy.notifications.http-token", () -> "test-token");
        registry.add("hreasy.notifications.worker.enabled", () -> "false");
        registry.add("hreasy.notifications.channels.yandex-messenger.enabled", () -> "true");
        registry.add("hreasy.notifications.channels.email.enabled", () -> "false");
    }

    @Autowired
    private DatabaseClient db;

    @BeforeEach
    void cleanDatabase() {
        db.sql("delete from notify_ms.notification_delivery").then()
                .then(db.sql("delete from notify_ms.notification").then())
                .block();
    }
}
