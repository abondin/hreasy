package ru.abondin.hreasy.platform.repo.salary;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

import java.time.OffsetDateTime;

@Data
@Table("sal.salary_request_approval")
@NoArgsConstructor
public class SalaryRequestApprovalEntry {
    @Id
    private Integer id;

    @NonNull
    private Integer requestId;

    /**
     * <ul>
     *     <li>0 - Comment: If no decision made. Just basic comment</li>
     *     <li>1 - Approved</li>
     *     <li>2 - Declined</li>
     * </ul>
     */
    @NonNull
    private Short state;
    private String comment;
    private OffsetDateTime createdAt;
    private Integer createdBy;
    private OffsetDateTime deletedAt;
    private Integer deletedBy;
}
