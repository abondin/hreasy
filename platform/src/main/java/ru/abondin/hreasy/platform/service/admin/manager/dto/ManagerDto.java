package ru.abondin.hreasy.platform.service.admin.manager.dto;

import lombok.Data;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.time.OffsetDateTime;

/**
 * Manager of the department, business account or project
 */
@Data
public class ManagerDto {

    @Data
    public static class ManagerResponsibilityObject {
        /**
         * One of ['project', 'business_account', 'department']
         */
        private String type;

        /**
         * project, ba or department id
         */
        private int id;

        /**
         * project, ba or department name
         */
        private String name;

        /**
         * project.baId or ba.id.
         * Uses to filter project or ba by ba id.
         */
        private Integer baId;
        /**
         * project.departmentId or department.id
         * Uses to filter project or department by department id.
         */
        private Integer departmentId;
    }

    private int id;

    private SimpleDictDto employee;


    /**
     * Link to project,ba or department
     */
    private ManagerResponsibilityObject responsibilityObject;

    /**
     * One of ['technical', 'organization', 'hr']
     */
    private String responsibilityType;

    /**
     * Comment in free form
     */
    private String comment;

    private OffsetDateTime createdAt;

    private Integer createdBy;
}
