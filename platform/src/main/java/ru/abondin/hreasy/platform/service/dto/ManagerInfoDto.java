package ru.abondin.hreasy.platform.service.dto;

import lombok.Data;

/**
 *
 * Public information about manager on project/ba/departments
 *
 * @see ru.abondin.hreasy.platform.service.admin.manager.dto.ManagerDto
 */
@Data
public class ManagerInfoDto {
    private int id;

    private int employeeId;

    private String employeeName;

    /**
     * One of ['technical', 'organization', 'hr']
     */
    private String responsibilityType;

    /**
     * Comment in free form
     */
    private String comment;
}
