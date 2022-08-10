package ru.abondin.hreasy.platform.service.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Information about employee responsible for ba or project
 */
@Data
public abstract class ResponsibleEmployeeDto {

    /**
     * Employee responsible for the technical part of the project/ba
     */
    public static final String TECHNICAL_TYPE = "technical";
    /**
     * Employee responsible for the organization part of the project/ba
     */
    public static final String ORGANIZATION_TYPE = "organization";

    private SimpleDictDto employee;
    /**
     * List of responsible types
     *
     * @see #TECHNICAL_TYPE
     * @see #ORGANIZATION_TYPE
     */
    private List<String> types = new ArrayList<>();
}
