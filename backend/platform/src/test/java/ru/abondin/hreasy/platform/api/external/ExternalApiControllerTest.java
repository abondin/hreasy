package ru.abondin.hreasy.platform.api.external;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.sec.UserDetailsWithEmployeeInfo;
import ru.abondin.hreasy.platform.service.EmployeeService;
import ru.abondin.hreasy.platform.service.FileStorage;
import ru.abondin.hreasy.platform.service.allocation.ResourceAllocationService;
import ru.abondin.hreasy.platform.service.dict.DictService;
import ru.abondin.hreasy.platform.service.overtime.OvertimeService;

import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExternalApiControllerTest {

    @Test
    void convertsIsoMonthToExistingZeroBasedOvertimePeriod() {
        var overtimeService = mock(OvertimeService.class);
        when(overtimeService.getSummary(anyInt(), any(AuthContext.class))).thenReturn(Flux.empty());
        var controller = new ExternalApiController(
                mock(EmployeeService.class),
                overtimeService,
                mock(ResourceAllocationService.class),
                mock(DictService.class),
                mock(FileStorage.class));

        var authorities = List.of(new SimpleGrantedAuthority("overtime_view"));
        var principal = new UserDetailsWithEmployeeInfo(
                new User("integration.user@example.test", "", authorities),
                1, null, null, List.of(), List.of(), List.of(),
                AuthContext.LoginType.EXTERNAL_API.getValue(), null);
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);

        StepVerifier.create(controller.overtimes(YearMonth.of(2026, 9))
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)))
                .verifyComplete();

        verify(overtimeService).getSummary(eq(202608), any(AuthContext.class));
    }
}
