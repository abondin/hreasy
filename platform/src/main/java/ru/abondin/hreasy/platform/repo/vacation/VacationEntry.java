package ru.abondin.hreasy.platform.repo.vacation;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;

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
    /**
     * 0 - planned
     * 1 - taken
     * 2 -
     * 3 - canceled
     */
    @Column("stat")
    private int status;

    private String documents;

    private int daysNumber;

    @Column("planned_start_date")
    private LocalDate plannedStartDate;
    @Column("planned_end_date")
    private LocalDate plannedEndDate;


    private Integer createdBy;
    private OffsetDateTime createdAt;

    private Integer updatedBy;
    private OffsetDateTime updatedAt;

    @Data
    public static class VacationHistoryEntry {
        @Id
        private Integer id;
        private int vacationId;
        private Integer employee;
        private Integer year;
        @Column("start_date")
        private LocalDate startDate;
        @Column("end_date")
        private LocalDate endDate;
        private String notes;
        /**
         * 0 - planning
         * 1 - taken
         * 2 - canceled
         */
        @Column("stat")
        private int status;

        private String documents;

        private int daysNumber;

        @Column("planned_start_date")
        private LocalDate plannedStartDate;
        @Column("planned_end_date")
        private LocalDate plannedEndDate;


        private Integer createdBy;
        private OffsetDateTime createdAt;
    }
}
