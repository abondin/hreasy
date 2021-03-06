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
import ru.abondin.hreasy.platform.service.admin.dto.UserSecurityInfoDto;
import ru.abondin.hreasy.platform.service.admin.dto.UserSecurityInfoMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminUserRolesService {

    private final UserRepo userRepo;
    private final EmployeeRepo employeeRepo;
    private final UserSecurityInfoMapper mapper;
    private final DateTimeService dateTimeService;
    private final AdminSecurityValidator validator;

    public Flux<UserSecurityInfoDto> users(AuthContext auth, @Nullable AdminUserRolesController.AdminUserListFilter filter) {
        var now = dateTimeService.now();
        return validator.validateGetEmployeeWithSecurity(auth).flatMapMany(b ->
                employeeRepo.findWithSecurityInfo().map(mapper::fromEntry).filter(employee -> {
                    if (filter == null && !filter.isIncludeFired()) {
                        return employee.getDateOfDismissal() == null || employee.getDateOfDismissal().isBefore(now.toLocalDate());
                    }
                    return true;
                }));
    }
}
