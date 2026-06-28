package ru.abondin.hreasy.platform.repo.employee.admin;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Column;

/**
 * Read-only view for EmployeeWithAllDetailsEntry.
 * Read from database additional (joined from another tables) information
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class EmployeeWithAllDetailsWithBaView extends EmployeeWithAllDetailsEntry{
    @Column("ba_id")
    private Integer baId;

}
