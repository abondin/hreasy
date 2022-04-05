package ru.abondin.hreasy.platform.repo.sec;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Table("sec.login_history")
@Data
public class SecLoginHistoryEntry {
    @Id
    private Integer id;
    private String login;
    private Integer loggedEmployeeId;
    private String error;
    private OffsetDateTime loginTime;
}
