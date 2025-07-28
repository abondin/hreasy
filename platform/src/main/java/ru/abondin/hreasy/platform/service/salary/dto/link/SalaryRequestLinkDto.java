package ru.abondin.hreasy.platform.service.salary.dto.link;

import lombok.Data;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

/**
 * Data to create a link between two salary requests
 */
@Data
public class SalaryRequestLinkDto {
    private int id;
    private boolean initiator;
    private SimpleDictDto linkedRequest;
    private SalaryRequestLinkType type;
    private String comment;
}
