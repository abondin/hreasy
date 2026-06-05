package ru.abondin.hreasy.platform.service.admin.dto;

import lombok.Data;
import lombok.ToString;
import org.springframework.lang.Nullable;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import org.springframework.lang.NonNull;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * DTO object for CRUD operations with Project Dictionary
 */
@Data
@ToString
public class ProjectDto {

    private int id;

    @NonNull
    private String name;

    @Nullable
    private String customer;

    @Nullable
    private LocalDate startDate;
    @Nullable
    private LocalDate endDate;

    @Nullable
    private LocalDate planStartDate;
    @Nullable
    private LocalDate planEndDate;

    @NonNull
    private SimpleDictDto department;

    @Nullable
    private SimpleDictDto businessAccount;

    private int createdBy;

    private OffsetDateTime createdAt;

    private boolean active;

    private String info;

    @Data
    @ToString
    public static class CreateOrUpdateProjectDto {
        @NonNull
        private String name;

        @Nullable
        private String customer;

        @Nullable
        private LocalDate startDate;
        @Nullable
        private LocalDate endDate;
        @Nullable
        private LocalDate planStartDate;
        @Nullable
        private LocalDate planEndDate;

        @NonNull
        private Integer departmentId;

        @Nullable
        private Integer baId;

        private String info;
    }
}
