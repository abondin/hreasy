package ru.abondin.hreasy.platform.service.admin.ba.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.abondin.hreasy.platform.service.ba.dto.BusinessAccountPositionDto;

import org.springframework.lang.NonNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class BusinessAccountPositionWithRateDto extends BusinessAccountPositionDto {
    /**
     * Rate in local currency
     */
    @NonNull
    private BigDecimal rate;
}
