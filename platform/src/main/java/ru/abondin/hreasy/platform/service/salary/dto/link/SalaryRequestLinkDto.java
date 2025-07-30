package ru.abondin.hreasy.platform.service.salary.dto.link;

import lombok.Data;
import org.springframework.lang.NonNull;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.time.OffsetDateTime;

/**
 * Data to create a link between two salary requests
 */
@Data
public class SalaryRequestLinkDto {
    private int id;
    private boolean initiator;
    @NonNull
    private SalaryLinkedRequest linkedRequest;
    /**
     * @see SalaryRequestLinkType
     */
    private short type;
    private String comment;
    @NonNull
    private OffsetDateTime createdAt;
    @NonNull
    private SimpleDictDto createdBy;

    @Data
    public static class SalaryLinkedRequest {
        private int id;
        private int period;
        private short implState;
        private OffsetDateTime createdAt;
        private SimpleDictDto createdBy;
    }
}
