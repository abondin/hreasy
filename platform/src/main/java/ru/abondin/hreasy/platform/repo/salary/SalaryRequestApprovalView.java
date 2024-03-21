package ru.abondin.hreasy.platform.repo.salary;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SalaryRequestApprovalView extends SalaryRequestApprovalEntry {
    private String createdByDisplayName;
}