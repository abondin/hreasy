package ru.abondin.hreasy.platform.service.admin.manager.dto;

import lombok.Data;

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
    }

    private int id;

    /**
     * Link to the employee
     */
    private int employee;


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
