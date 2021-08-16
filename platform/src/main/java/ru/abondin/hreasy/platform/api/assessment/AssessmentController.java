package ru.abondin.hreasy.platform.api.assessment;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.assessment.AssessmentService;
import ru.abondin.hreasy.platform.service.assessment.dto.AssessmentDto;
import ru.abondin.hreasy.platform.service.assessment.dto.EmployeeAssessmentsSummary;

/**
 * View last assessment for every employee.
 * Create assessment.
 * Fill assessment form.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/assessment")
public class AssessmentController {

    private final AssessmentService service;

    @GetMapping
    public Flux<EmployeeAssessmentsSummary> allNotFiredEmployeesWithLatestAssessment() {
        return AuthHandler.currentAuth().flatMapMany(auth -> service.allNotFiredEmployeesWithLatestAssessment(auth));
    }


    @GetMapping("/{employeeId}")
    public Flux<AssessmentDto> assessmentBase(@PathVariable("employeeId") int employeeId) {
        return AuthHandler.currentAuth().flatMapMany(auth -> service.employeeAssessments(auth, employeeId));
    }


}
