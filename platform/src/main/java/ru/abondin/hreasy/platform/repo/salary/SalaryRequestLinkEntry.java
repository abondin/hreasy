package ru.abondin.hreasy.platform.repo.salary;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@Table("sal.salary_request_link")
@NoArgsConstructor
public class SalaryRequestLinkEntry {
    @Id
    private Integer id;
    private Integer type;
    private Integer source;
    private Integer destination;
    private String comment;
    private Integer createdBy;
    private OffsetDateTime createdAt;
    private Integer deletedBy;
    private OffsetDateTime deletedAt;
}
