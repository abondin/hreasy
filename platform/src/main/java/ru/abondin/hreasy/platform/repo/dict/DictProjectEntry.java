package ru.abondin.hreasy.platform.repo.dict;

import io.r2dbc.postgresql.codec.Json;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;
import ru.abondin.hreasy.platform.service.dto.ManagerInfoDto;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Table("proj.project")
@Data
public class DictProjectEntry {
    @Id
    private Integer id;

    @NotNull
    private String name;

    @Nullable
    private String customer;

    @Nullable
    private String personOfContact;

    @Nullable
    private LocalDate startDate;
    @Nullable
    private LocalDate endDate;

    @NotNull
    private Integer departmentId;

    @Nullable
    private Integer baId;

    @NotNull
    private OffsetDateTime createdAt;

    @NotNull
    private Integer createdBy;

    private String info;


    @Data
    public static class ProjectFullEntry extends DictProjectEntry {
        @NotNull
        private String departmentName;
        private String baName;
    }

    @Data
    public static class ProjectFullEntryWithManagers extends ProjectFullEntry {
        private Json managersJson;
    }


    @Table("proj.project_history")
    @Data
    public static class ProjectHistoryEntry {
        @Id
        private Integer id;

        @NotNull
        private Integer projectId;

        @NotNull
        private String name;

        @Nullable
        private String customer;

        @Nullable
        private String personOfContact;

        @Nullable
        private LocalDate startDate;
        @Nullable
        private LocalDate endDate;

        @NotNull
        private Integer departmentId;

        @Nullable
        private Integer baId;

        @NotNull
        private OffsetDateTime updatedAt;

        @NotNull
        private Integer updatedBy;
    }
}
