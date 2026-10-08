package ru.abondin.hreasy.platform.service.admin.ba;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountEntry;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountHistoryRepo;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.AdminSecurityValidator;
import ru.abondin.hreasy.platform.service.admin.ba.dto.CreateOrUpdateBABody;
import ru.abondin.hreasy.platform.service.ba.dto.BusinessAccountMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminBusinessAccountServiceTest {
    @Test
    void replacesKeyAndClearsNullOrEmptyValues() {
        var repo = mock(BusinessAccountRepo.class);
        var history = mock(BusinessAccountHistoryRepo.class);
        var security = mock(AdminSecurityValidator.class);
        var service = new AdminBusinessAccountService(repo, history, Mappers.getMapper(BusinessAccountMapper.class),
                security, new DateTimeService());
        var auth = new AuthContext("admin", "admin@example.test", List.of(),
                new AuthContext.EmployeeInfo(201, null, null, List.of(), List.of(), List.of(), null, null));
        var entry = new BusinessAccountEntry();
        entry.setId(501);
        entry.setExternalId("account-alpha");
        when(security.validateAddOrUpdateBusinessAccount(auth)).thenReturn(Mono.just(true));
        when(repo.findById(501)).thenReturn(Mono.just(entry));
        when(repo.save(any())).thenAnswer(call -> Mono.just(call.getArgument(0)));
        when(history.save(any())).thenAnswer(call -> Mono.just(call.getArgument(0)));

        var body = new CreateOrUpdateBABody();
        body.setName("Example account");
        StepVerifier.create(service.update(auth, 501, body)).expectNext(501).verifyComplete();
        assertNull(entry.getExternalId());

        entry.setExternalId("account-alpha");
        body.setExternalId("");
        StepVerifier.create(service.update(auth, 501, body)).expectNext(501).verifyComplete();
        assertNull(entry.getExternalId());
        assertEquals("", body.getExternalId());

        body.setExternalId("  account-beta  ");
        StepVerifier.create(service.update(auth, 501, body)).expectNext(501).verifyComplete();
        assertEquals("account-beta", entry.getExternalId());
        verify(history).save(argThat(saved -> "account-beta".equals(saved.getExternalId())));
    }
}
