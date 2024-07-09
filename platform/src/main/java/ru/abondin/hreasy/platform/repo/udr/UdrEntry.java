package ru.abondin.hreasy.platform.repo.udr;

import io.r2dbc.postgresql.codec.Json;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@Table(value = "registry", schema = "udr")
public class UdrEntry {
    @Id
    private Integer id;

    @Column("name")
    private String name;

    @Column("owner_id")
    private Integer ownerId;

    private String description;

    @Column("employee_fields")
    private String[] employeeFields;

    @Column("custom_fields")
    private Json customFields;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("created_by")
    private Integer createdBy;

    @Column("deleted_at")
    private OffsetDateTime deletedAt;

    @Column("deleted_by")
    private Integer deletedBy;
}