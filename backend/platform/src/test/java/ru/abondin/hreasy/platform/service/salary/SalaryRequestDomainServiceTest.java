package ru.abondin.hreasy.platform.service.salary;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.assessment.AssessmentRepo;
import ru.abondin.hreasy.platform.repo.employee.EmployeeDetailedRepo;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestClosedPeriodRepo;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestLinkRepo;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.HistoryDomainService;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestMapper;
import ru.abondin.hreasy.platform.service.salary.dto.link.SalaryRequestLinkCreateBody;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SalaryRequestDomainServiceTest {

    @Test
    void rejectsEqualRequestIdsOutsideIntegerCache() {
        var securityValidator = mock(SalarySecurityValidator.class);
        when(securityValidator.validateReportSalaryRequest(any())).thenReturn(Mono.just(true));
        var service = new SalaryRequestDomainService(
                mock(DateTimeService.class),
                mock(SalaryRequestRepo.class),
                securityValidator,
                mock(SalaryRequestMapper.class),
                mock(EmployeeDetailedRepo.class),
                mock(HistoryDomainService.class),
                mock(AssessmentRepo.class),
                mock(SalaryRequestClosedPeriodRepo.class),
                mock(SalaryRequestLinkRepo.class));

        StepVerifier.create(service.validateLinkCreation(
                        mock(AuthContext.class),
                        new SalaryRequestLinkCreateBody(1000, 1000, (short) 1, null)))
                .expectErrorMatches(error -> error instanceof BusinessError businessError
                        && "errors.salary_request.link.source_equals_destination".equals(businessError.getCode()))
                .verify();
    }
}
