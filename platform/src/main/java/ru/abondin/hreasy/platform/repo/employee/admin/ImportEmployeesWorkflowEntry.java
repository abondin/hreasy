package ru.abondin.hreasy.platform.repo.employee.admin;


import io.r2dbc.postgresql.codec.Json;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@Table("empl.import_workflow")
public class ImportEmployeesWorkflowEntry {
    private Integer id;
    private Integer createdBy;
    private OffsetDateTime createdAt;
    private Integer approvedBy;
    private OffsetDateTime approvedAt;
    private OffsetDateTime appliedAt;
    private Json data;
}
