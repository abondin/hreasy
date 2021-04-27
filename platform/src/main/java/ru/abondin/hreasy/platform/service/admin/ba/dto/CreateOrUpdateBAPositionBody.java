package ru.abondin.hreasy.platform.service.admin.ba.dto;

import lombok.Data;
import lombok.ToString;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@ToString
public class CreateOrUpdateBAPositionBody {
    @NotNull
    private String name;
    @Nullable
    private Integer businessAccountId;
    @Nullable
    private String description;
    @NotNull
    private BigDecimal rate;

}
