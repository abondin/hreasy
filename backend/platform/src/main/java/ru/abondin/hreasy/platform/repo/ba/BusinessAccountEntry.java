package ru.abondin.hreasy.platform.repo.ba;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

/**
 * Business account that generate a profit
 */
@Data
@Table("ba.business_account")
public class BusinessAccountEntry {
    @Id
    private Integer id;
    private String name;
    private String description;
    private boolean archived;
    private OffsetDateTime createdAt;
    private Integer createdBy;
}
