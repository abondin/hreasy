package ru.abondin.hreasy.platform.repo.vacation;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDate;

@Data
public class VacationView {
    private Integer id;
    private Integer employee;
    @Column("employee_firstname")
    private String employeeFirstname;
    @Column("employee_lastname")
    private String employeeLastname;
    @Column("employee_patronymic_name")
    private String employeePatronymicName;
    private Integer year;
    @Column("start_date")
    private LocalDate startDate;
    @Column("end_date")
    private LocalDate endDate;
    private String notes;
}
