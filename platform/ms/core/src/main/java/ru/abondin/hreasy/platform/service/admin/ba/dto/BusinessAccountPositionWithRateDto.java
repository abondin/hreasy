package ru.abondin.hreasy.platform.service.admin.ba.dto;

import lombok.Data;
import ru.abondin.hreasy.platform.service.ba.dto.BusinessAccountPositionDto;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class BusinessAccountPositionWithRateDto extends BusinessAccountPositionDto {
    /**
     * Rate in local currency
     */
    @NotNull
    private BigDecimal rate;
}
