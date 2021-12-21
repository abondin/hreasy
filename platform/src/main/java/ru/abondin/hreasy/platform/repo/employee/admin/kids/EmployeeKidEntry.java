package ru.abondin.hreasy.platform.repo.employee.admin.kids;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * All employee kids table for HR to add or update
 */
@Data
@Table("empl.kids")
public class EmployeeKidEntry {
    @Id
    private Integer id;

    @NotNull
    @Column("name")
    private String displayName;

    private LocalDate birthday;

    @NotNull
    private Integer parent;
}
