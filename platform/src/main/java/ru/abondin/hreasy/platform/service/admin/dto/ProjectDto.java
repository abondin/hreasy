package ru.abondin.hreasy.platform.service.admin.dto;

import lombok.Data;
import lombok.ToString;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * DTO object for CRUD operations with Project Dictionary
 */
@Data
@ToString
public class ProjectDto {

    private int id;

    @NotNull
    private String name;

    @Null
    private String customer;

    @Null
    private LocalDate startDate;
    @Null
    private LocalDate endDate;

    @NotNull
    private SimpleDictDto department;

    private int createdBy;

    private OffsetDateTime createdAt;

    @Data
    @ToString
    public static class CreateOrUpdateProjectDto {
        @NotNull
        private String name;

        @Null
        private String customer;

        @Null
        private LocalDate startDate;
        @Null
        private LocalDate endDate;

        @NotNull
        private Integer departmentId;
    }
}
