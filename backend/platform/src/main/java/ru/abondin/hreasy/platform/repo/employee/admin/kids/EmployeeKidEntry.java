package ru.abondin.hreasy.platform.repo.employee.admin.kids;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import org.springframework.lang.NonNull;
import java.time.LocalDate;

/**
 * All employee kids table for HR to add or update
 */
@Data
@NoArgsConstructor
@Table("empl.kids")
public class EmployeeKidEntry {
    @Id
    private Integer id;

    @NonNull
    @Column("name")
    private String displayName;

    private LocalDate birthday;

    @NonNull
    private Integer parent;
}
