package ru.abondin.hreasy.platform.repo.employee.admin.imp;


import io.r2dbc.postgresql.codec.Json;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.ImportEmployeeExcelRowDto;

import java.time.OffsetDateTime;

@Data
@Table("empl.import_workflow")
public class ImportWorkflowEntry {

    public static int STATE_CREATED = 0;
    public static int STATE_FILE_UPLOADED = 1;
    public static int STATE_CONFIGURATION_SET = 2;
    public static int STATE_CHANGES_APPLIED = 3;
    public static int STATE_ABORTED = -1;

    public static short WF_TYPE_EMPLOYEE = 1;
    public static short WF_TYPE_KIDS = 2;


    @Id
    private Integer id;

    private OffsetDateTime createdAt;
    private Integer createdBy;

    private OffsetDateTime completedAt;
    private Integer completedBy;


    private OffsetDateTime configSetAt;
    private Integer configSetBy;

    private int state = 0;
    /**
     * 1 - employee, 2 - kids
     */
    private short wfType;


    private String filename;
    private Long fileContentLength;

    /**
     * Configuration to import data from file
     *
     * @See EmployeeImportConfig
     */
    private Json config;

    /**
     * Imported data
     *
     * @see ImportEmployeeExcelRowDto
     */
    private Json importedRows;

    private Json importProcessStats;
}
