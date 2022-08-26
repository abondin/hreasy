package ru.abondin.hreasy.platform.service.dto;

import lombok.Data;

/**
 *
 * Public information about manager on project/ba/departments
 *
 * @see ru.abondin.hreasy.platform.service.admin.manager.dto.ManagerDto
 */
@Data
public class ManagerInfoDto extends SimpleDictDto{
    /**
     * One of ['technical', 'organization', 'hr']
     */
    private String responsibilityType;

    /**
     * Comment in free form
     */
    private String comment;
}
