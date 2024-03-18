package ru.abondin.hreasy.platform.service.salary.dto.approval;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SalaryRequestApproveBody {
    private String comment;
}

