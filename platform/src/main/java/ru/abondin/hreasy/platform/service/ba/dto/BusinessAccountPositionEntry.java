package ru.abondin.hreasy.platform.service.ba.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Business account position that generates profit
 */
@Data
public class BusinessAccountPositionEntry {
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
