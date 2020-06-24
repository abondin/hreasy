package ru.abondin.hreasy.platform.repo.vacation;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Data
@Table("vacation")
public class VacationEntry {
    @Id
    private Integer id;
    private Integer employee;
    private Integer year;
    @Column("start_date")
    private LocalDate startDate;
    @Column("end_date")
    private LocalDate endDate;
    private String notes;
}
