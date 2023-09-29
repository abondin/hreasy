package ru.abondin.hreasy.platform.service.salary.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum SalaryRequestStat {
    /**
     * When PM creates a request
     */
    CREATED((short) 0),

    /**
     * Salary Manager or Finance marks after Finance implements
     */
    IMPLEMENTED((short) 1),

    /**
     * When Salary Manager reject request
     */
    REJECTED((short) -1);
    private final short value;

}
