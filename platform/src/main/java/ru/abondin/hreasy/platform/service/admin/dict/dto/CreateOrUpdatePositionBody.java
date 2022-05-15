package ru.abondin.hreasy.platform.service.admin.dict.dto;

import lombok.Data;

/**
 * Employee's position in the company
 */
@Data
public class CreateOrUpdatePositionBody {
    /**
     * Name of the employee's position in official HR documents.
     */
    private String name;
    /**
     * Simple name like Developer or analyst
     */
    private String category;

    private boolean archived = false;

}
