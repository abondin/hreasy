package ru.abondin.hreasy.platform.repo.employee;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.repo.sec.SecPasswdEntry;
import ru.abondin.hreasy.platform.repo.sec.SecPasswdRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;

import java.time.OffsetDateTime;

/**
 * Simple aggregation for basic {@link EmployeeRepo} methods to provide security information for given employee
 */
@Service
@RequiredArgsConstructor
public class EmployeeAuthDomainService {

    public record TelegramToEmailBinding(
            String email,
            String telegramAccount,
            boolean telegramConfirmed
    ) {
    }

    private final EmployeeRepo employeeRepo;
    private final SecPasswdRepo passwdRepo;
    private final DateTimeService dateTimeService;

    public Mono<TelegramToEmailBinding> findEmailByTelegramAccount(String telegramAccount, boolean onlyConfirmed) {
        return employeeRepo.findEmailByTelegramAccount(telegramAccount)
                .map(b -> new TelegramToEmailBinding(b.getEmail(), telegramAccount,
                        b.getTelegramConfirmedAt() != null && b.getTelegramConfirmedAt().isBefore(OffsetDateTime.now())));
    }

    public Mono<EmployeeAuthInfoEntry> findNotDismissedByUsername(String email) {
        return employeeRepo.findNotDismissedIdByEmail(email, dateTimeService.now()).flatMap(empl -> {
            var entry = new EmployeeAuthInfoEntry();
            entry.setId(empl.getId());
            entry.setDepartmentId(empl.getDepartmentId());
            entry.setCurrentProjectId(empl.getCurrentProjectId());
            return employeeRepo.findAccessibleDepartments(empl.getId()).collectList()
                    .flatMap(deps -> employeeRepo.findAccessibleProjects(empl.getId()).collectList()
                            .flatMap(projects -> employeeRepo.findAccessibleBas(empl.getId()).collectList()
                                    .map(bas -> {
                                        entry.setAccessibleDepartments(deps);
                                        entry.setAccessibleProjects(projects);
                                        entry.setAccessibleBas(bas);
                                        return entry;
                                    })));
        });
    }

    public Mono<String> getInternalPassword(int employeeId) {
        return passwdRepo.findByEmployee(employeeId).map(SecPasswdEntry::getPasswordHash);
    }
}
