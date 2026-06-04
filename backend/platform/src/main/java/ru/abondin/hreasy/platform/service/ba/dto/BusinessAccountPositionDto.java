package ru.abondin.hreasy.platform.service.ba.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import org.springframework.lang.NonNull;

/**
 * Business account position that generates profit
 */
@Data
@NoArgsConstructor
public class BusinessAccountPositionDto {
    @NonNull
    private Integer id;
    @NonNull
    private int businessAccount;
    @NonNull
    private String name;
    @Nullable
    private String description;
    private boolean archived=false;
}
