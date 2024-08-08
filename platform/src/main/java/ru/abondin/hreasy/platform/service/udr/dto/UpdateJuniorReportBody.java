package ru.abondin.hreasy.platform.service.udr.dto;

import lombok.Data;

@Data
public class UpdateJuniorReportBody {
    private int progress;
    private String comment;
}
