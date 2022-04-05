package ru.abondin.hreasy.platform.repo.overtime;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

/**
 * Exported to disk excel document for overtimes for the period
 */
@Data
@Table("ovt.overtime_export_document")
public class OvertimeExportedDocumentEntry {

    @Id
    private Integer id;

    @NotNull
    private int periodId;

    @NotNull
    private String filename;

    /**
     * Current logged in user
     */
    @NotNull
    private int exportedBy;


    @NotNull
    private OffsetDateTime exportedAt;

    @Nullable
    private String comment;
}
