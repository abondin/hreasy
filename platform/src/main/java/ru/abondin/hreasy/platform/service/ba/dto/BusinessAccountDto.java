package ru.abondin.hreasy.platform.service.ba.dto;

import lombok.Data;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Business account that generate a profit
 */
@Data
public class BusinessAccountDto {
    @NotNull
    private Integer id;
    @Nullable
    private String name;
    private List<BusinessAccountResponsibleEmployeeDto> responsibleEmployees = new ArrayList<>();
    private String description;
    private boolean archived = false;
    @NotNull
    private OffsetDateTime createdAt;
    @NotNull
    private Integer createdBy;
}
