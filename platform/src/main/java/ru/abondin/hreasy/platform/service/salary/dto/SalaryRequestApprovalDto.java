package ru.abondin.hreasy.platform.service.salary.dto;

import lombok.Data;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import org.springframework.lang.NonNull;
import java.time.OffsetDateTime;

@Data
public class SalaryRequestApprovalDto {
    private Integer id;
    @NonNull
    private Short stat;
    private String comment;
    private OffsetDateTime createdAt;
    private SimpleDictDto createdBy;
}
