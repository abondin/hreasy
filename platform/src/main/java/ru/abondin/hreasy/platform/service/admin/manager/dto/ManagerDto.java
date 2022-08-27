package ru.abondin.hreasy.platform.service.admin.manager.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Manager of the department, business account or project
 */
@Data
public class ManagerDto {

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public enum ManagerResponsibilityObjectType {
        PROJECT("project"), BUSINESS_ACCOUNT("business_account"), DEPARTMENT("department");
        private final String name;

        public static ManagerResponsibilityObjectType byName(String name) {
            return Arrays.stream(values()).filter(v -> v.name.equals(name))
                    .findFirst().orElseThrow(() -> new RuntimeException("Unexpected ResponsibilityObjectType " + name));
        }
    }

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
