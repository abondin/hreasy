package ru.abondin.hreasy.platform.service.admin.manager;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.manager.ManagerEntry;
import ru.abondin.hreasy.platform.service.admin.AdminSecurityValidator;

import java.util.List;

import static org.mockito.Mockito.mock;

class ManagerSecurityValidatorTest {
    private final ManagerSecurityValidator validator = new ManagerSecurityValidator(mock(AdminSecurityValidator.class));

    @Test
    void allowsCreatorAndAdminToDeleteManagerLink() {
        var manager = managerCreatedBy(1000);

        StepVerifier.create(validator.validateDeleteManager(auth(1000, List.of()), manager))
                .expectNext(true)
                .verifyComplete();
        StepVerifier.create(validator.validateDeleteManager(auth(8, List.of("admin_managers")), manager))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void rejectsAnotherProjectEditor() {
        StepVerifier.create(validator.validateDeleteManager(auth(8, List.of()), managerCreatedBy(7)))
                .expectError(AccessDeniedException.class)
                .verify();
    }

    private ManagerEntry managerCreatedBy(int employeeId) {
        var manager = new ManagerEntry();
        manager.setCreatedBy(employeeId);
        return manager;
    }

    private AuthContext auth(int employeeId, List<String> authorities) {
        return new AuthContext("user", "user@example.test", authorities,
                new AuthContext.EmployeeInfo(employeeId, null, null, List.of(), List.of(), List.of(), null, null));
    }
}
