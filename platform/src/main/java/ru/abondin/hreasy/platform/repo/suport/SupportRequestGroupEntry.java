package ru.abondin.hreasy.platform.repo.suport;

import io.r2dbc.postgresql.codec.Json;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;

@Table(name = "support_request_group", schema = "support")
@Data
public class SupportRequestGroupEntry implements Persistable<String> {

    /**
     * Implicit say to insert entry, not update because we always have prepopulated key
     */
    @Transient
    private boolean isNew = true;

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
    private OffsetDateTime createdAt;

    @Column("created_by")
    private Integer createdBy;

    @Column("deleted_at")
    private OffsetDateTime deletedAt;

    @Column("deleted_by")
    private Integer deletedBy;

    @Nullable
    @Override
    public String getId() {
        return key;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}

