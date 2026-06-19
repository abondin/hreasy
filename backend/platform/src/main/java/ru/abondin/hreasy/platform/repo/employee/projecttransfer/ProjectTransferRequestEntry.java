package ru.abondin.hreasy.platform.repo.employee.projecttransfer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@Table("empl.project_transfer_request")
public class ProjectTransferRequestEntry {
    public static final short STATE_PENDING = 1;
    public static final short STATE_APPROVED = 2;
    public static final short STATE_REJECTED = 3;
    public static final short STATE_CANCELED = 4;
    public static final short STATE_EXPIRED = 5;

    @Id
    private Integer id;

    private Integer employeeId;
    private Integer fromProjectId;
    private Integer toProjectId;
    private String requestedProjectRole;
    private Integer approverEmployeeId;
    private Short state;
    private String decisionComment;
    private Integer appliedEmployeeHistoryId;
    private OffsetDateTime createdAt;
    private Integer createdBy;
    private OffsetDateTime updatedAt;
    private Integer updatedBy;
}
