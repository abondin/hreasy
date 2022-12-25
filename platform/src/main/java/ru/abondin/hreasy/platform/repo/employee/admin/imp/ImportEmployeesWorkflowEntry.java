package ru.abondin.hreasy.platform.repo.employee.admin.imp;


import io.r2dbc.postgresql.codec.Json;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import ru.abondin.hreasy.platform.service.admin.employee.dto.ImportEmployeeExcelDto;

import java.time.OffsetDateTime;

@Data
@Table("empl.import_workflow")
public class ImportEmployeesWorkflowEntry {
    @Id
    private Integer id;

    private OffsetDateTime createdAt;
    private Integer createdBy;

    private OffsetDateTime completedAt;
    private Integer completedBy;


    private int state = 0;

    private String filename;
    private Long fileContentLength;

    /**
     * Configuration to import data from file
     * @See EmployeeImportConfig
     */
    private Json config;

    /**
     * Imported data
     * @see ImportEmployeeExcelDto
     */
    private Json data;
}
