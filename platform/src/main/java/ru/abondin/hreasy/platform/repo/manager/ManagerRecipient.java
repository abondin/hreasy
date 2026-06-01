package ru.abondin.hreasy.platform.repo.manager;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;

/**
 * Active manager selected as a business recipient for employee-related workflows.
 */
@Data
public class ManagerRecipient {
    @Column("employee_id")
    private Integer employeeId;
    private String email;
    @Column("display_name")
    private String displayName;
}
