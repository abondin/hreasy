package ru.abondin.hreasy.platform.repo.sec;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Table("sec.passwd")
@Data
public class SecPasswdEntry {
    @Id
    private int employee;

    private String passwordHash;

    private OffsetDateTime createdAt;
    private Integer createdBy;
}
