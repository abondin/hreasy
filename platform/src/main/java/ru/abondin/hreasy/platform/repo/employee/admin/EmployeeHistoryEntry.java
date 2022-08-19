package ru.abondin.hreasy.platform.repo.employee.admin;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

/**
 * All employee table for HR to add or update employee information
 */
@Data
@Table("empl.employee_history")
public class EmployeeHistoryEntry extends EmployeeWithAllDetailsEntry {
    private int employee;
    private OffsetDateTime createdAt;
    private int createdBy;
}
