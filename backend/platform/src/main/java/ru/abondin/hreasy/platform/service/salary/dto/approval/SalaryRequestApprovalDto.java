package ru.abondin.hreasy.platform.service.salary.dto.approval;

import lombok.*;
import org.springframework.lang.NonNull;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class SalaryRequestApprovalDto {
    private Integer id;

    private Integer requestId;

    /**
     * 1 - Comment: If no decision made. Just basic comment
     * 2 - Approved
     * 3 - Declined
     */
    @NonNull
    private Short state;
    private String comment;
    private OffsetDateTime createdAt;
    private SimpleDictDto createdBy;

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public enum ApprovalActionTypes {
        COMMENT((short) 1), APPROVE((short) 2), DECLINE((short) 3);
        private final short value;
    }
}
