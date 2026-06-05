package ru.abondin.hreasy.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.mapping.R2dbcMappingContext;

/**
 * Configures R2DBC mapping for schema-qualified table names used by platform entities.
 */
@Configuration
public class R2dbcMappingConfig {
    /**
     * Keeps {@code @Table("schema.table")} rendered as {@code schema.table} instead of quoted
     * {@code "schema.table"} names.
     *
     * @return R2DBC mapping context with quoting disabled
     */
    @Bean
    R2dbcMappingContext r2dbcMappingContext() {
        return R2dbcMappingContext.forPlainIdentifiers();
    }
}
