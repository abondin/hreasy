package ru.abondin.hreasy.platform.service.udr.dto;

import lombok.Data;

@Data
public class UpdateJuniorRegistryBody {
    private Integer mentorId;
    private String role;
    private Integer budgetingAccount;
}
