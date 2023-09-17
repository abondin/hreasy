package ru.abondin.hreasy.platform.service.salary.dto;

import lombok.Data;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Data
public class SalaryRequestApprovalDto {
    private Integer id;
    @NotNull
    private Short stat;
    private String comment;
    private OffsetDateTime createdAt;
    private SimpleDictDto createdBy;
}
