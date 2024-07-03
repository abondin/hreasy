package ru.abondin.hreasy.platform.repo.suport;

import io.r2dbc.postgresql.codec.Json;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "support_request_group", schema = "support")
@Data
public class SupportRequestGroupEntry {
    @Id
    @Column("key")
    private String key;

    @Column("display_name")
    private String displayName;

    @Column("description")
    private String description;

    @Column("configuration")
    private Json configuration;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("created_by")
    private Long createdBy;

    @Column("deleted_at")
    private LocalDateTime deletedAt;

    @Column("deleted_by")
    private Long deletedBy;
}

