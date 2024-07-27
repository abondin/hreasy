package ru.abondin.hreasy.platform.repo.udr;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@Table(value = "junior_registry", schema = "udr")
public class JuniorEntry {
    @Id
    private Integer juniorId;

    private String role;

    private Integer mentorId;

    private Integer budgetingAccount;

    private OffsetDateTime createdAt;
    private Integer createdBy;

    private OffsetDateTime graduatedAt;
    private Integer graduatedBy;
    private String graduatedComment;
}