package ru.abondin.hreasy.platform.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.api.admin.AdminUserRolesController;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.employee.EmployeeRepo;
import ru.abondin.hreasy.platform.repo.sec.UserRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.dto.EmployeeWithSecurityInfoDto;
import ru.abondin.hreasy.platform.service.admin.dto.EmployeeWithSecurityMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminUserRolesService {

    private final UserRepo userRepo;
    private final EmployeeRepo employeeRepo;
    private final EmployeeWithSecurityMapper mapper;
    private final DateTimeService dateTimeService;
    private final AdminSecurityValidator validator;

    public Flux<EmployeeWithSecurityInfoDto> users(AuthContext auth, @Nullable AdminUserRolesController.AdminUserListFilter filter) {
        var now = dateTimeService.now();
        return validator.validateGetEmployeeWithSecurity(auth).flatMapMany(b ->
                employeeRepo.findWithSecurityInfo().map(mapper::fromEntry).filter(employee -> {
                    if (filter == null && !filter.isIncludeFired()) {
                        return employee.getDateOfDismissal() == null || employee.getDateOfDismissal().isAfter(now.toLocalDate());
                    }
                    return true;
                }));
    }
}
