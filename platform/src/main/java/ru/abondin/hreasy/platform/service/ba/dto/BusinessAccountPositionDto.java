package ru.abondin.hreasy.platform.service.ba.dto;

import lombok.Data;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;

/**
 * Business account position that generates profit
 */
@Data
public class BusinessAccountPositionDto {
    @NotNull
    private Integer id;
    @NotNull
    private int businessAccount;
    @NotNull
    private String name;
    @Nullable
    private String description;
    private boolean archived=false;
}
