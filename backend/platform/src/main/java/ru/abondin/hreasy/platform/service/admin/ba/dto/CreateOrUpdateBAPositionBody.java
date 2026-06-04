package ru.abondin.hreasy.platform.service.admin.ba.dto;

import lombok.Data;
import lombok.ToString;
import org.springframework.lang.Nullable;

import org.springframework.lang.NonNull;
import java.math.BigDecimal;

@Data
@ToString
public class CreateOrUpdateBAPositionBody {
    @NonNull
    private String name;
    @Nullable
    private String description;
    @NonNull
    private BigDecimal rate;

    private boolean archived = false;
}
