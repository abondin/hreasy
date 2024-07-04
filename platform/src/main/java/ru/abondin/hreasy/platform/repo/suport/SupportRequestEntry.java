package ru.abondin.hreasy.platform.repo.suport;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@Table(name = "support_request", schema = "support")
public class SupportRequestEntry {

    @Id
    @Column("id")
    private Integer id;

    @Column("employee_id")
    private Long employeeId;

    @Column("support_group")
    private String supportGroup;

    @Column("source_type")
    private Integer sourceType;

    @Column("message")
    private String message;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("created_by")
    private Integer createdBy;

    @Column("deleted_at")
    private OffsetDateTime deletedAt;

    @Column("deleted_by")
    private Integer deletedBy;
}