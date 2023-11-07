package ru.abondin.hreasy.platform.repo.employee;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Table("empl.employee")
public class EmployeeEntry {
    @Id
    private Integer id;
    private String displayName;
    private LocalDate birthday;
    private String sex;
    @Column("department")
    private Integer departmentId;
    @Column("position")
    private Integer positionId;
    @Column("current_project")
    private Integer currentProjectId;
    @Column("current_project_role")
    private String currentProjectRole;
    @Column("office_location")
    private Integer officeLocationId;
    @Column("date_of_employment")
    private LocalDate dateOfEmployment;
    @Column("date_of_dismissal")
    private LocalDate dateOfDismissal;
    private String email;
    private String phone;
    private String skype;
    private String telegram;
}
