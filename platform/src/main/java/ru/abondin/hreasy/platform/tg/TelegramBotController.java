package ru.abondin.hreasy.platform.tg;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.EmployeeService;
import ru.abondin.hreasy.platform.service.admin.manager.AdminManagerService;
import ru.abondin.hreasy.platform.service.vacation.VacationService;
import ru.abondin.hreasy.platform.tg.dto.TgMapper;
import ru.abondin.hreasy.platform.tg.dto.TgMyProfileDto;

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
    private final TgMapper mapper;

    @Operation(summary = "Send employee's telegram account confirmation email")
    @PostMapping(value = "confirm/start")
    public Mono<String> sendConfirmationLink() {
        return AuthHandler.currentAuth().flatMap(confirmService::sendConfirmationLink);
    }


    @Operation(summary = "Get my information")
    @GetMapping(value = "my-profile")
    public Mono<TgMyProfileDto> myProfile() {
        return AuthHandler.currentAuth().flatMap(auth ->
                // 1. Load employee
                emplService.find(auth.getEmployeeInfo().getEmployeeId(), auth)
                        .map(mapper::myProfile)
                        // 2. Load upcoming vacations
                        .flatMap(employee ->
                                vacationService.currentOrFutureVacations(employee.getId(), auth)
                                        .map(v -> new TgMyProfileDto.VacationDto(v.getStartDate(), v.getEndDate()))
                                        .collectList()
                                        .flatMap(vacations -> {
                                            employee.setUpcomingVacations(vacations);
                                            if (employee.getProjectId() == null) {
                                                return Mono.just(employee);
                                            } else {
                                                // 3. Load project manager
                                                return managerService.byObject(auth, "project", employee.getProjectId())
                                                        .map(m -> m.getEmployee().getName())
                                                        .collectList()
                                                        .map(managers -> {
                                                            employee.setProjectManagers(managers);
                                                            return employee;
                                                        });
                                            }
                                        })));
    }
}
