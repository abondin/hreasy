package ru.abondin.hreasy.platform.service.admin.employee;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeWithAllDetailsRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.AdminSecurityValidator;
import ru.abondin.hreasy.platform.service.admin.employee.dto.CreateOrUpdateEmployeeBody;
import ru.abondin.hreasy.platform.service.admin.employee.dto.EmployeeAllFieldsMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminEmployeeService {
    private final DateTimeService dateTimeService;
    private final EmployeeWithAllDetailsRepo employeeRepo;
    private final AdminSecurityValidator securityValidator;
    private final EmployeeAllFieldsMapper mapper;


    public Mono<Integer> create(AuthContext auth, CreateOrUpdateEmployeeBody body) {
        log.info("Create new employee {} by {}", body, auth.getUsername());
        return securityValidator.validateEditEmployee(auth)
                .map(sec -> mapper.fromCreateOrUpdate(body))
                .flatMap(entry -> employeeRepo.save(entry).map(e -> e.getId()));
    }

    public Mono<Integer> update(AuthContext auth, int employeeId, CreateOrUpdateEmployeeBody body) {
        log.info("Update new employee {} with {} by {}", employeeId, body, auth.getUsername());
        return securityValidator.validateEditEmployee(auth)
                .map(sec -> mapper.fromCreateOrUpdate(body))
                .flatMap(entry -> employeeRepo.save(entry).map(e -> e.getId()));
    }

}
