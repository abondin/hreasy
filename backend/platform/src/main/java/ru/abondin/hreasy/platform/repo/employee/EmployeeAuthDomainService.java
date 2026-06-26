package ru.abondin.hreasy.platform.repo.employee;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.repo.manager.ManagerRepo;
import ru.abondin.hreasy.platform.repo.sec.SecPasswdEntry;
import ru.abondin.hreasy.platform.repo.sec.SecPasswdRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;

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
    private final ManagerRepo managerRepo;

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
            return Mono.zip(
                            employeeRepo.findAccessibleDepartments(empl.getId()).collectList(),
                            employeeRepo.findAccessibleProjects(empl.getId()).collectList(),
                            employeeRepo.findAccessibleBas(empl.getId()).collectList(),
                            managerRepo.findManagedDepartmentIds(empl.getId()).collectList(),
                            managerRepo.findManagedProjectIds(empl.getId()).collectList(),
                            managerRepo.findManagedBaIds(empl.getId()).collectList())
                    .map(access -> {
                        entry.setManagedDepartments(access.getT4());
                        entry.setManagedProjects(access.getT5());
                        entry.setManagedBas(access.getT6());
                        entry.setAccessibleDepartments(mergeAccess(access.getT1(), access.getT4()));
                        entry.setAccessibleProjects(mergeAccess(access.getT2(), access.getT5()));
                        entry.setAccessibleBas(mergeAccess(access.getT3(), access.getT6()));
                        return entry;
                    });
        });
    }

    private List<Integer> mergeAccess(List<Integer> manualAccess, List<Integer> managerAccess) {
        var result = new LinkedHashSet<Integer>();
        result.addAll(manualAccess);
        result.addAll(managerAccess);
        return result.stream().toList();
    }

    public Mono<String> getInternalPassword(int employeeId) {
        return passwdRepo.findByEmployee(employeeId).map(SecPasswdEntry::getPasswordHash);
    }
}
