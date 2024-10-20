package ru.abondin.hreasy.platform.tg;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.EmployeeService;
import ru.abondin.hreasy.platform.service.admin.manager.AdminManagerService;
import ru.abondin.hreasy.platform.service.support.SupportRequestService;
import ru.abondin.hreasy.platform.service.support.dto.NewSupportRequestDto;
import ru.abondin.hreasy.platform.service.support.dto.SupportRequestGroupDto;
import ru.abondin.hreasy.platform.service.vacation.VacationService;
import ru.abondin.hreasy.platform.tg.dto.*;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Static Markdown articles (news for example)
 */
@RestController()
@RequestMapping("/telegram/api/v1")
@RequiredArgsConstructor
@Slf4j
public class TelegramBotController {
    private final EmployeeService emplService;
    private final VacationService vacationService;
    private final AdminManagerService managerService;
    private final TelegramConfirmService confirmService;
    private final TelegramFindEmployeesService findEmployeesService;
    private final TgMapper mapper;
    private final SupportRequestService supportRequestService;

    @Operation(summary = "Send employee's telegram account confirmation email")
    @PostMapping(value = "confirm/start")
    public Mono<String> sendConfirmationLink() {
        return AuthHandler.currentAuth().flatMap(confirmService::sendConfirmationLink);
    }


    @Operation(summary = "Get my information")
    @GetMapping(value = "my-profile")
    public Mono<TgMyProfileResponse> myProfile() {
        return AuthHandler.currentAuth().flatMap(auth ->
                // 1. Load employee
                emplService.find(auth.getEmployeeInfo().getEmployeeId(), auth)
                        .map(mapper::myProfile)
                        // 2. Load upcoming vacations
                        .flatMap(employee -> addUpcomingVacations(employee))
                        // 3. Load project managers
                        .flatMap(employee -> addProjectManagers(employee, auth)));
    }

    @Operation(summary = "Find one or more employees by email or display name")
    @PostMapping(value = "employee/find/{employeeId}")
    public Mono<FindEmployeeResponse.EmployeeDto> find(@PathVariable int employeeId) {
        return AuthHandler.currentAuth().flatMap(auth ->
                emplService.find(employeeId, auth)
                        .map(mapper::toFindEmployeeResponseEmployee)
                        // 2. Load upcoming vacations
                        .flatMap(employee -> addUpcomingVacations(employee)));
    }


    @Operation(summary = "Find one or more employees by email or display name")
    @PostMapping(value = "employee/find")
    public Mono<FindEmployeeResponse> find(@RequestBody FindEmployeeRequest query, Locale locale) {
        return AuthHandler.currentAuth().flatMap(auth -> findEmployeesService.find(auth, query
                        , Optional.ofNullable(locale).orElse(Locale.getDefault()))
                .flatMap(response -> {
                    List<FindEmployeeResponse.EmployeeDto> employees = response.getEmployees();
                    // Load vacations for each employee
                    return Flux.fromIterable(employees)
                            .flatMap(employee -> addUpcomingVacations(employee)
                            )
                            .collectList()
                            .map(updatedEmployees -> {
                                response.setEmployees(updatedEmployees);
                                return response;
                            });
                }));
    }

    @Operation(description = "Get all not deleted support request groups")
    @GetMapping("support/request/groups")
    public Flux<SupportRequestGroupDto> supportRequestGroups() {
        return AuthHandler.currentAuth().flatMapMany(supportRequestService::groups);
    }

    /**
     * Support Group CRUD API is not implemented.
     * Please populate <b>support.support_request_group</b> manually in SQL Editor.
     *
     * @param request
     * @return
     */
    @Operation(description = "Get all not deleted support request groups")
    @PostMapping("support/request")
    public Mono<Integer> postNewSupportRequest(@RequestBody NewSupportRequestDto request) {
        return AuthHandler.currentAuth().flatMap(auth ->
                supportRequestService.createSupportRequest(auth, NewSupportRequestDto.SOURCE_TYPE_TELEGRAM, request));
    }

    private Mono<FindEmployeeResponse.EmployeeDto> addUpcomingVacations(FindEmployeeResponse.EmployeeDto employee) {
        return vacationService.currentOrFutureVacations(employee.getId())
                .map(v -> new VacationDto(v.getStartDate(), v.getEndDate()))
                .collectList()
                .map(vacations -> {
                    employee.setUpcomingVacations(vacations);
                    return employee;
                });
    }

    private Mono<TgMyProfileResponse> addUpcomingVacations(TgMyProfileResponse employee) {
        return vacationService.currentOrFutureVacations(employee.getId())
                .map(v -> new VacationDto(v.getStartDate(), v.getEndDate()))
                .collectList()
                .map(vacations -> {
                    employee.setUpcomingVacations(vacations);
                    return employee;
                });
    }

    private Mono<TgMyProfileResponse> addProjectManagers(TgMyProfileResponse employee, AuthContext auth) {
        if (employee.getProjectId() == null) {
            return Mono.just(employee);
        }
        return managerService.byObject(auth, "project", employee.getProjectId())
                .map(m -> m.getEmployee().getName())
                .collectList()
                .map(managers -> {
                    employee.setProjectManagers(managers);
                    return employee;
                });
    }

}
