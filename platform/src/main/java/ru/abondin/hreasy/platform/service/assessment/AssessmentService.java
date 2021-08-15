package ru.abondin.hreasy.platform.service.assessment;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.assessment.AssessmentRepo;
import ru.abondin.hreasy.platform.repo.assessment.EmployeeAssessmentEntry;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.assessment.dto.AssessmentShortInfoDto;

import java.util.ArrayList;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final AssessmentSecurityValidator securityValidator;
    private final AssessmentRepo assessmentRepo;
    private final DateTimeService dateTimeService;
    private final AssessmentMapper mapper;

    public Flux<AssessmentShortInfoDto> allNotFiredEmployeesWithLatestAssessment(AuthContext auth) {
        var now = dateTimeService.now();
        return securityValidator.validateCanCreateAssessment(auth).thenMany(
                assessmentRepo.findNotCanceledAssessmentForNotFired(now)
                        .collectMultimap(EmployeeAssessmentEntry::getEmployeeId)
                        .map(multimap -> {
                            var result = new ArrayList<AssessmentShortInfoDto>();
                            for (var employeeId : multimap.keySet()) {
                                var latestByPlannedDate = multimap.get(employeeId)
                                        .stream()
                                        .sorted(Comparator.comparing(EmployeeAssessmentEntry::getPlannedDate, Comparator.nullsLast(Comparator.naturalOrder()))
                                                .reversed()).findFirst().get();
                                result.add(mapper.shortInfo(latestByPlannedDate));
                            }
                            return result;
                        }).flatMapMany(Flux::fromIterable)
        );
    }
}


