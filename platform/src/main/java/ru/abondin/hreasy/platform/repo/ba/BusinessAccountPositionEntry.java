package ru.abondin.hreasy.platform.repo.ba;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Business account position that generates profit
 */
@Data
@Table("ba_position")
public class BusinessAccountPositionEntry {
    @Id
    private Integer id;
    private int businessAccount;
    private String name;
    private String description;
    /**
     * Rate in local currency
     */
    private BigDecimal rate;
    private OffsetDateTime archivedAt;
    private Integer archivedBy;
    private OffsetDateTime createdAt;
    private Integer createdBy;
}
