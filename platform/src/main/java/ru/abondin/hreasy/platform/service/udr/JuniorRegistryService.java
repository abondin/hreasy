package ru.abondin.hreasy.platform.service.udr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.abondin.hreasy.platform.repo.employee.EmployeeRepo;
import ru.abondin.hreasy.platform.repo.udr.JuniorRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.udr.dto.JuniorReportMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class JuniorRegistryService {

    private final DateTimeService dateTimeService;
    private final EmployeeRepo employeeRepo;
    private final JuniorRepo udrRepo;
    private final JuniorSecurityValidator securityValidator;
    private final JuniorReportMapper mapper;


}
