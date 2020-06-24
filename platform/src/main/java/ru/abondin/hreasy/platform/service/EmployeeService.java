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
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.employee.EmployeeRepo;
import ru.abondin.hreasy.platform.service.dto.EmployeeDto;
import ru.abondin.hreasy.platform.service.mapper.EmployeeDtoMapper;

import java.util.Arrays;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeDtoMapper mapper;
    private final DateTimeService dateTimeService;
    private final EmployeeRepo emplRepo;

    public Flux<EmployeeDto> findAll(AuthContext auth, boolean includeFired) {
        log.debug("Find all employees from {} account", auth.getEmail());
        //TODO Add filtering and ordering
        Criteria criteria = null;
        if (!includeFired) {
            addNotFiredCriteria(criteria);
        }
        return emplRepo.findDetailed(criteria,
                Sort.by("lastname", "firstname", "patronymicName")
        ).map(e -> mapper.employeeToDto(e));
    }


    public Mono<EmployeeDto> find(int employeeId, AuthContext auth) {
        log.debug("Find {} employee from {} account", employeeId, auth.getEmail());
        return emplRepo.findDetailed(employeeId)
                .map(e -> mapper.employeeToDto(e))
                .switchIfEmpty(Mono.error(new HttpClientErrorException(HttpStatus.NOT_FOUND,
                        "Employee with ID=" + employeeId + " not found")));
    }

    public Mono<Boolean> updateCurrentProject(int employeeId, int newCurrentProjectId, AuthContext auth) {
        log.info("Update current project {} for employee {}" +
                "by {}", newCurrentProjectId, employeeId, auth.getEmail());
        return emplRepo.updateCurrentProject(employeeId, newCurrentProjectId).map(updatedRowsCount -> updatedRowsCount > 0);
    }

    public Mono<Boolean> updateCurrentProject(int newCurrentProjectId, AuthContext auth) {
        if (auth.getEmployeeInfo() == null) {
            throw new BusinessError("errors.no.employee.for.auth", Arrays.asList(auth.getEmail()));
        }
        log.info("Update own current project {} - {} ({})", newCurrentProjectId, auth.getEmail(), auth.getEmployeeInfo().getEmployeeId());
        return emplRepo.updateCurrentProject(auth.getEmployeeInfo().getEmployeeId(), newCurrentProjectId).map(updatedRowsCount -> updatedRowsCount > 0);
    }


    private Criteria addNotFiredCriteria(Criteria criteria) {
        var notFiredCriteria = criteria == null ? Criteria.where("date_of_dismissal")
                : criteria.and("date_of_dismissal");
        return notFiredCriteria.isNull().or("date_of_dismissal").greaterThan(dateTimeService.now());

    }
}
