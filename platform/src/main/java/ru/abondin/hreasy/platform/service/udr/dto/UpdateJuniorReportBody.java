package ru.abondin.hreasy.platform.service.udr.dto;

import lombok.Data;

@Data
public class UpdateJuniorReportBody {
    /**
     * @see JuniorReportDto#getProgress()
     */
    private int progress;
    private String comment;
    private JuniorReportRatings ratings = new JuniorReportRatings();
}
