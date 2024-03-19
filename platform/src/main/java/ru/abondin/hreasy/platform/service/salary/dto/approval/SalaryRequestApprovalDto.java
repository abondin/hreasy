package ru.abondin.hreasy.platform.service.salary.dto.approval;

import lombok.Data;
import org.springframework.lang.NonNull;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.time.OffsetDateTime;

@Data
public class SalaryRequestApprovalDto {
    private Integer id;
    /**
     * 1 - Comment: If no decision made. Just basic comment
     * 2 - Approved
     * 3 - Declined
     */
    @NonNull
    private Short state;
    @NonNull
    private String comment;
    private OffsetDateTime createdAt;
    private SimpleDictDto createdBy;

    public static enum ApprovalActionTypes {
        COMMENT((short) 1), APPROVE((short) 2), DECLINE((short) 3);
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
