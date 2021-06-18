package ru.abondin.hreasy.platform.repo.employee.admin;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import ru.abondin.hreasy.platform.repo.employee.EmployeeEntry;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * All employee table for HR to add or update employee information
 */
@Data
@Table("employee_history")
public class EmployeeHistoryEntry extends EmployeeWithAllDetailsEntry {
    private int employee;
    private OffsetDateTime createdAt;
    private int createdBy;
}
