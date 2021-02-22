package ru.abondin.hreasy.platform.repo.dict;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Table("project")
@Data
public class DictProjectEntry {
    @Id
    private Integer id;

    @NotNull
    private String name;

    @Null
    private String customer;

    @Nullable
    private Integer personOfContact;

    @Null
    private LocalDate startDate;
    @Null
    private LocalDate endDate;

    @NotNull
    private Integer departmentId;

    @NotNull
    private OffsetDateTime createdAt;

    @NotNull
    private Integer createdBy;


    @Table("project_history")
    @Data
    public static class ProjectHistoryEntry {
        @Id
        private Integer historyId;

        @NotNull
        private Integer projectId;

        @NotNull
        private String name;

        @Null
        private String customer;

        @Nullable
        private Integer personOfContact;

        @Null
        private LocalDate startDate;
        @Null
        private LocalDate endDate;

        @NotNull
        private Integer departmentId;

        @NotNull
        private OffsetDateTime updatedAt;

        @NotNull
        private Integer updatedBy;

        @Nullable
        private String comment;
    }
}
