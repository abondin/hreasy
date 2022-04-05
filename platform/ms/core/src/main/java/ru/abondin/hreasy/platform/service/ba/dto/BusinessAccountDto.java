package ru.abondin.hreasy.platform.service.ba.dto;

import lombok.Data;
import org.springframework.lang.Nullable;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

/**
 * Business account that generate a profit
 */
@Data
public class BusinessAccountDto {
    @NotNull
    private Integer id;
    @Nullable
    private String name;
    private SimpleDictDto responsibleEmployee;
    private String description;
    private boolean archived = false;
    @NotNull
    private OffsetDateTime createdAt;
    @NotNull
    private Integer createdBy;
}
