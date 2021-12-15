package ru.abondin.hreasy.platform.repo.employee.admin;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import ru.abondin.hreasy.platform.repo.employee.EmployeeEntry;

import java.time.LocalDate;

/**
 * All employee table for HR to add or update employee information
 */
@Data
@Table("empl.employee")
public class EmployeeWithAllDetailsEntry extends EmployeeEntry {
    @Column("date_of_employment")
    private LocalDate dateOfEmployment;
    @Column("level")
    private Integer levelId;
    @Column("work_type")
    private String workType;
    @Column("work_day")
    private String workDay;
    @Column("registration_address")
    private String registrationAddress;
    @Column("document_series")
    private String documentSeries;
    @Column("document_number")
    private String documentNumber;
    @Column("document_issued_by")
    private String documentIssuedBy;
    @Column("document_issued_date")
    private LocalDate documentIssuedDate;
    @Column("foreign_passport")
    private String foreignPassport;
    @Column("city_of_residence")
    private String cityOfResidence;
    @Column("english_level")
    private String englishLevel;
    @Column("family_status")
    private String familyStatus;
    @Column("spouse_name")
    private String spouseName;
    private String children;
}
