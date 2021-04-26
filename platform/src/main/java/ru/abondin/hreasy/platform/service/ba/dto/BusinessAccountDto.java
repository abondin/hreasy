package ru.abondin.hreasy.platform.service.ba.dto;

import lombok.Data;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.time.OffsetDateTime;

/**
 * Business account that generate a profit
 */
@Data
public class BusinessAccountDto {
    private Integer id;
    private String name;
    private SimpleDictDto responsibleEmployee;
    private String description;
    private OffsetDateTime archivedAt;
    private Integer archivedBy;
    private OffsetDateTime createdAt;
    private Integer createdBy;
}
