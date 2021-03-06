package ru.abondin.hreasy.platform.repo.employee;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Table("employee")
public class EmployeeEntry {
    @Id
    private Integer id;
    private String lastname;
    private String firstname;

    @Column("patronymic_name")
    private String patronymicName;
    private LocalDate birthday;
    private String sex;
    @Column("department")
    private Integer departmentId;
    @Column("position")
    private Integer positionId;
    @Column("current_project")
    private Integer currentProjectId;
    @Column("office_location")
    private Integer officeLocationId;
    private LocalDate dateOfDismissal;
    private String email;
    //TODO Set phone datatype in database to string
    private BigDecimal phone;
    private String skype;
}
