package ru.abondin.hreasy.platform.repo.salary;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import org.springframework.lang.NonNull;
import java.time.OffsetDateTime;

@Data
@Table("sal.salary_request")
public class SalaryRequestApprovalEntry {
    @Id
    private Integer id;

    @NonNull
    private Integer request;

    /**
     * <ul>
     *     <li>0 - Comment: If no decision made. Just basic comment</li>
     *     <li>1 - Approved</li>
     *     <li>2 - Declined</li>
     * </ul>
     */
    @NonNull
    private Short stat;
    private String comment;
    private OffsetDateTime createdAt;
    private Integer createdBy;
    private OffsetDateTime deletedAt;
    private Integer deletedBy;
}
