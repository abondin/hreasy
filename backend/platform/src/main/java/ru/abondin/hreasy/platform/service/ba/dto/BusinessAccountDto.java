package ru.abondin.hreasy.platform.service.ba.dto;

import lombok.Data;
import org.springframework.lang.Nullable;
import ru.abondin.hreasy.platform.service.dto.ManagerInfoDto;

import org.springframework.lang.NonNull;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Business account that generate a profit
 */
@Data
public class BusinessAccountDto {
    @NonNull
    private Integer id;
    @Nullable
    private String name;
    private List<ManagerInfoDto> managers = new ArrayList<>();
    private String description;
    private boolean archived = false;
    @NonNull
    private OffsetDateTime createdAt;
    @NonNull
    private Integer createdBy;
}
