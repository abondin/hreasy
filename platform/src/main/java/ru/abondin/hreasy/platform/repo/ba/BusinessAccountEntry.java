package ru.abondin.hreasy.platform.repo.ba;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

/**
 * Business account that generate a profit
 */
@Data
@Table("business_account")
public class BusinessAccountEntry {
    @Id
    private Integer id;
    private String name;
    private Integer responsibleEmployee;
    private String description;
    private OffsetDateTime archivedAt;
    private Integer archivedBy;
    private OffsetDateTime createdAt;
    private Integer createdBy;
}
