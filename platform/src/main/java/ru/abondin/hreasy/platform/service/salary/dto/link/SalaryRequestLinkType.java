package ru.abondin.hreasy.platform.service.salary.dto.link;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum SalaryRequestLinkType {
    RESCHEDULED(1, "Rescheduled From", "Rescheduled To"),
    MULTISTAGE(2, "Part of Multi-Stage Increase", "Part of Multi-Stage Increase");
    private final int id;
    private final String sourceType;
    private final String destinationType;

    public static SalaryRequestLinkType fromId(int id) {
        for (SalaryRequestLinkType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown link type id: " + id);
    }
}
