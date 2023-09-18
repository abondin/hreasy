package ru.abondin.hreasy.platform.service.salary.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum SalaryRequestReportStat {
    /**
     * When PM creates a request
     */
    CREATED((short) 0),

    /**
     * When Salary Manager move it to in progress<
     */
    IN_PROGRESS((short) 1),

    /**
     * Salary Manager or Finance marks after Finance implements
     */
    IMPLEMENTED((short) 2);
    private final short value;

}
