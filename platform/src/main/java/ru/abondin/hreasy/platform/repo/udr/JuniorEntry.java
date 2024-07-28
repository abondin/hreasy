package ru.abondin.hreasy.platform.repo.udr;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;

@Data
@Table(value = "junior_registry", schema = "udr")
public class JuniorEntry implements Persistable<Integer> {
    /**
     * Implicit say to insert entry, not update because we always have prepopulated key
     */
    @Transient
    private boolean isNew = true;

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

    private OffsetDateTime deletedAt;
    private Integer deletedBy;


    @Nullable
    @Override
    public Integer getId() {
        return juniorId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}