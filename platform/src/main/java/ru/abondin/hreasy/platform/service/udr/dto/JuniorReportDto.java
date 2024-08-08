package ru.abondin.hreasy.platform.service.udr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.time.OffsetDateTime;

@Data
public class JuniorReportDto {
    private Integer id;
    /**
     * <ul>
     * <li>1 - Degradation</li>
     * <li>2 - No Progress</li>
     * <li>3 - Progress</li>
     * <li>4 - Good Progress</li>
     * </ul>
     */
    private short progress;
    private String comment;

    @NotNull
    private SimpleDictDto createdBy;
    private OffsetDateTime createdAt;
}
