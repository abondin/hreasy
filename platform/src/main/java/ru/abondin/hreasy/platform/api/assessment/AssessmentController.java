package ru.abondin.hreasy.platform.api.assessment;


import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.assessment.AssessmentService;
import ru.abondin.hreasy.platform.service.assessment.dto.AssessmentDto;
import ru.abondin.hreasy.platform.service.assessment.dto.AssessmentWithFormsAndFiles;
import ru.abondin.hreasy.platform.service.assessment.dto.EmployeeAssessmentsSummary;
import ru.abondin.hreasy.platform.service.assessment.dto.UploadAssessmentAttachmentResponse;

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
    public Flux<EmployeeAssessmentsSummary> findAllNotFiredEmployeesWithLatestAssessment() {
        return AuthHandler.currentAuth().flatMapMany(auth -> service.allNotFiredEmployeesWithLatestAssessment(auth));
    }


    @GetMapping("/{employeeId}")
    public Flux<AssessmentDto> getAssessmentBase(@PathVariable("employeeId") int employeeId) {
        return AuthHandler.currentAuth().flatMapMany(auth -> service.employeeAssessments(auth, employeeId));
    }

    @GetMapping("/{employeeId}/{assessmentId}")
    public Mono<AssessmentWithFormsAndFiles> getAssessmentWithFormsAndFiles(@PathVariable("employeeId") int employeeId,
                                                                         @PathVariable("assessmentId") int assessmentId) {
        return AuthHandler.currentAuth().flatMap(auth ->
                service.getAssessment(auth, assessmentId));
    }


    @Operation(summary = "Upload assessment attachment")
    @PostMapping(value = "{assessmentId}/attachment")
    public Mono<UploadAssessmentAttachmentResponse> uploadAttachment(@PathVariable int assessmentId,
                                                                     @RequestPart("file") Mono<FilePart> multipartFile) {
        log.debug("Upload new attachment {} for assessment {}", assessmentId);
        return AuthHandler.currentAuth().flatMap(auth -> multipartFile
                .flatMap(it -> service.uploadAttachment(auth, assessmentId, it)));
    }


}
