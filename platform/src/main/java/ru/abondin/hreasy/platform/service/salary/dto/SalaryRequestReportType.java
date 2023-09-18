package ru.abondin.hreasy.platform.service.salary.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum SalaryRequestReportType {
    /**
     * Salary increase
     */
    SALARY_INCREASE((short)1),
    /**
     * One time bonus
     */
    BONUS((short)2);
    private final short value;
}
