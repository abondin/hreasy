package ru.abondin.hreasy.platform.service.assessment;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.assessment.AssessmentRepo;
import ru.abondin.hreasy.platform.repo.assessment.EmployeeAssessmentEntry;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.assessment.dto.AssessmentDto;
import ru.abondin.hreasy.platform.service.assessment.dto.EmployeeAssessmentsSummary;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final AssessmentSecurityValidator securityValidator;
    private final AssessmentRepo assessmentRepo;
    private final DateTimeService dateTimeService;
    private final AssessmentMapper mapper;

    public Flux<EmployeeAssessmentsSummary> allNotFiredEmployeesWithLatestAssessment(AuthContext auth) {
        var now = dateTimeService.now();
        return securityValidator.validateCanCreateAssessment(auth).thenMany(
                assessmentRepo.findNotCanceledAssessmentForNotFired(now)
                        .collectMultimap(EmployeeAssessmentEntry::getEmployeeId)
                        .map(multimap -> {
                            var result = new ArrayList<EmployeeAssessmentsSummary>();
                            for (var employeeId : multimap.keySet()) {
                                var latestByPlannedDate = multimap.get(employeeId)
                                        .stream()
                                        .sorted(Comparator.comparing(EmployeeAssessmentEntry::getPlannedDate, Comparator.nullsLast(Comparator.naturalOrder()))
                                                .reversed()).findFirst().get();
                                var dto = mapper.shortInfo(latestByPlannedDate);
                                dto.setDaysWithoutAssessment(dto.getLatestActivity() == null ? null :
                                        ChronoUnit.DAYS.between(dto.getLatestActivity(), now));
                                result.add(dto);
                            }
                            return result;
                        }).flatMapMany(Flux::fromIterable)
        );
    }

    public Flux<AssessmentDto> employeeAssessments(AuthContext auth, int employeeId) {
        return securityValidator.validateCanCreateAssessment(auth).thenMany(
                assessmentRepo.findByEmployeeId(employeeId).map(mapper::assessmentBaseFromEntry));
    }
}


