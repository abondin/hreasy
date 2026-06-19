package ru.abondin.hreasy.platform.service.admin.ba.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.abondin.hreasy.platform.service.ba.dto.BusinessAccountPositionDto;

import org.springframework.lang.NonNull;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class BusinessAccountPositionWithRateDto extends BusinessAccountPositionDto {
    /**
     * Rate in local currency
     */
    @NonNull
    private BigDecimal rate;
}
