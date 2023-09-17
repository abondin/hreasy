package ru.abondin.hreasy.platform.service.salary;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestApprovalRepo;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestRepo;

@Service
@RequiredArgsConstructor
public class SalaryRequestService {
    private final SalaryRequestRepo requestRepo;
    private final SalaryRequestApprovalRepo approvalRepo;
    private final SalarySecurityValidator secValidator;



}
