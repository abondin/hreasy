package ru.abondin.hreasy.platform.repo.sec;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Table("sec.passwd_history")
@Data
public class SecPasswdHistoryEntry {
    @Id
    private int id;

    private int employee;

    private String passwordHash;

    private OffsetDateTime createdAt;
    private Integer createdBy;
}
