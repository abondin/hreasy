package ru.abondin.hreasy.platform.repo.manager;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

/**
 * Manager of the department, business account or project
 */
@Data
@Table("empl.manager")
public class ManagerEntry {
    @Id
    private int id;

    private int employee;

    /**
     * project, business_account, department
     */
    private String objectType;

    private int objectId;

    private String responsibilityType;

    private String comment;

    private OffsetDateTime createdAt;

    private Integer createdBy;
}
