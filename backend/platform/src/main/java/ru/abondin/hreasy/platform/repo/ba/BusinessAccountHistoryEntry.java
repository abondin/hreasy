package ru.abondin.hreasy.platform.repo.ba;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@Table("ba.business_account_history")
public class BusinessAccountHistoryEntry {
    @Id
    private Integer id;
    private int baId;
    private String name;
    private Integer responsibleEmployee;
    private String description;
    private boolean archived;
    private OffsetDateTime updatedAt;
    private Integer updatedBy;
}
