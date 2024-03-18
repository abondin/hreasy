package ru.abondin.hreasy.platform.service.salary.dto.approval;

import lombok.Data;
import org.springframework.lang.NonNull;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.time.OffsetDateTime;

@Data
public class SalaryRequestApprovalDto {
    private Integer id;
    /**
     * 0 - Comment: If no decision made. Just basic comment
     * 1 - Approved
     * 2 - Declined
     */
    @NonNull
    private Short stat;
    @NonNull
    private String comment;
    private OffsetDateTime createdAt;
    private SimpleDictDto createdBy;

    public static enum ApprovalActionTypes {
        COMMENT((short) 0), APPROVE((short) 1), DECLINE((short) 2);
        private final short value;

        ApprovalActionTypes(short value) {
            this.value = value;
        }

        public short getValue() {
            return this.value;
        }

        public static ApprovalActionTypes fromValue(short value) {
            for (ApprovalActionTypes type : ApprovalActionTypes.values()) {
                if (type.getValue() == value) {
                    return type;
                }
            }
            throw new UnsupportedOperationException("Unsupported value: " + value);
        }
    }
}
