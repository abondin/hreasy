package ru.abondin.hreasy.platform.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.employee.EmployeeRepo;
import ru.abondin.hreasy.platform.service.currentproject.EmployeeProjectSecurityValidator;
import ru.abondin.hreasy.platform.service.dto.CurrentProjectRole;
import ru.abondin.hreasy.platform.service.dto.EmployeeDto;
import ru.abondin.hreasy.platform.service.dto.EmployeeProjectChangesDto;
import ru.abondin.hreasy.platform.service.mapper.EmployeeDtoMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeDtoMapper mapper;
    private final DateTimeService dateTimeService;
    private final EmployeeRepo emplRepo;
    private final EmployeeProjectSecurityValidator employeeProjectSecurityValidator;

    public Flux<EmployeeDto> findAll(AuthContext auth, boolean includeFired) {
        log.debug("Find all employees from {} account", auth.getEmail());
        Criteria criteria = null;
        if (!includeFired) {
            criteria = addNotFiredCriteria(criteria);
        }
        return emplRepo.findDetailed(criteria,
                        Sort.by("display_name")
                ).map(e -> mapper.employeeWithSkills(e, auth.getEmployeeInfo().getEmployeeId()))
                .doOnNext(empl -> employeeProjectSecurityValidator.setToNullUngrantedFields(empl, auth));
    }


    public Mono<EmployeeDto> find(int employeeId, AuthContext auth) {
        log.debug("Find {} employee from {} account", employeeId, auth.getEmail());
        return emplRepo.findDetailed(employeeId)
                .map(e -> mapper.employeeWithSkills(e, auth.getEmployeeInfo().getEmployeeId()))
                .switchIfEmpty(Mono.error(new HttpClientErrorException(HttpStatus.NOT_FOUND,
                        "Employee with ID=" + employeeId + " not found")))
                .doOnNext(empl -> employeeProjectSecurityValidator.setToNullUngrantedFields(empl, auth));
    }


    private Criteria addNotFiredCriteria(Criteria criteria) {
        var dateOfDismissalColumn = "date_of_dismissal";
        var notFiredCriteria = criteria == null ? Criteria.where(dateOfDismissalColumn)
                : criteria.and(dateOfDismissalColumn);
        return notFiredCriteria.isNull().or(dateOfDismissalColumn).greaterThan(dateTimeService.now());

    }

    public Flux<CurrentProjectRole> currentUserRoles(AuthContext auth) {
        log.debug("Find all current user roles by {}", auth.getUsername());
        return emplRepo.uniqueCurrentProjectRoles(dateTimeService.now()).map(CurrentProjectRole::new);
    }

    public Flux<EmployeeProjectChangesDto> employeeProjectChanges(AuthContext auth, int employeeId) {
        log.debug("Find project changes for employee {} by ", employeeId, auth.getUsername());
        return emplRepo.employeeProjectChanges(employeeId).map(mapper::employeeProjectChangesFromEntry);
    }
}
