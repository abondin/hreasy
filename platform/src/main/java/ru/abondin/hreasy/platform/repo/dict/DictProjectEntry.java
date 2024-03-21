package ru.abondin.hreasy.platform.repo.dict;

import io.r2dbc.postgresql.codec.Json;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;
import ru.abondin.hreasy.platform.service.dto.ManagerInfoDto;

import org.springframework.lang.NonNull;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Table("proj.project")
@NoArgsConstructor
@Data
public class DictProjectEntry {
    @Id
    private Integer id;

    @NonNull
    private String name;

    @Nullable
    private String customer;

    @Nullable
    private String personOfContact;

    @Nullable
    private LocalDate startDate;
    @Nullable
    private LocalDate endDate;

    @NonNull
    private Integer departmentId;

    @Nullable
    private Integer baId;

    @NonNull
    private OffsetDateTime createdAt;

    @NonNull
    private Integer createdBy;

    private String info;


    @Data
    @NoArgsConstructor
    public static class ProjectFullEntry extends DictProjectEntry {
        @NonNull
        private String departmentName;
        private String baName;
    }

    @Data
    @NoArgsConstructor
    public static class ProjectFullEntryWithManagers extends ProjectFullEntry {
        private Json managersJson;
    }


    @Table("proj.project_history")
    @Data
    public static class ProjectHistoryEntry {
        @Id
        private Integer id;

        @NonNull
        private Integer projectId;

        @NonNull
        private String name;

        @Nullable
        private String customer;

        @Nullable
        private String personOfContact;

        @Nullable
        private LocalDate startDate;
        @Nullable
        private LocalDate endDate;

        @NonNull
        private Integer departmentId;

        @Nullable
        private Integer baId;

        @NonNull
        private OffsetDateTime updatedAt;

        @NonNull
        private Integer updatedBy;
    }
}
