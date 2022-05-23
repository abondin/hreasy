package ru.abondin.hreasy.platform.repo.employee.admin;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;

/**
 * Read-only view for EmployeeWithAllDetailsEntry.
 * Read from database additional (joined from another tables) information
 */
@Data
public class EmployeeWithAllDetailsWithBaView extends EmployeeWithAllDetailsEntry{
    @Column("ba_id")
    private Integer baId;

}
