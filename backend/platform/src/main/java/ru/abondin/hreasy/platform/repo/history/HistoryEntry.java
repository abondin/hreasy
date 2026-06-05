package ru.abondin.hreasy.platform.repo.history;

import io.r2dbc.postgresql.codec.Json;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

/**
 * HR 1.3 will be store all entities changes in one historical table.
 * In 1.2 only new entities will be logged in that way
 * For big installations this table can be partitioned by entity_type and year
 */
@Data
@Table("history.history")
public class HistoryEntry {
    @Id
    private int id;

    private String entityType;

    private int entityId;

    private Json entityValue;

    private OffsetDateTime createdAt;

    private Integer createdBy;
}
