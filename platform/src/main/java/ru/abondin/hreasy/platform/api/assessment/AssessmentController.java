package ru.abondin.hreasy.platform.api.assessment;


import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.assessment.AssessmentService;
import ru.abondin.hreasy.platform.service.assessment.dto.*;

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
                service.getAssessment(auth, employeeId, assessmentId));
    }

    @Operation(summary = "Schedule new assessment")
    @PostMapping(value = "/{employeeId}")
    public Mono<Integer> scheduleAssessment(@PathVariable("employeeId") int employeeId,
                                            @RequestBody CreateAssessmentDto body) {
        log.debug("Schedule new assessment for {}:{}", employeeId, body);
        return AuthHandler.currentAuth().flatMap(auth -> service.scheduleAssessment(auth, employeeId, body));
    }


    @Operation(summary = "Cancel  assessment")
    @DeleteMapping(value = "/{employeeId}/{assessmentId}")
    public Mono<Integer> cancelAssessment(@PathVariable("employeeId") int employeeId,
                                          @PathVariable("assessmentId") int assessmentId) {
        log.debug("Cancel assessment {}:{}", employeeId, assessmentId);
        return AuthHandler.currentAuth().flatMap(auth -> service.cancelAssessment(auth, employeeId, assessmentId));
    }

    @Operation(summary = "Complete  assessment")
    @PostMapping(value = "/{employeeId}/{assessmentId}/complete")
    public Mono<Integer> completeAssessment(@PathVariable("employeeId") int employeeId,
                                          @PathVariable("assessmentId") int assessmentId) {
        log.debug("Mark assessment as completed {}:{}", employeeId, assessmentId);
        return AuthHandler.currentAuth().flatMap(auth -> service.completeAssessment(auth, employeeId, assessmentId));
    }

    @Operation(summary = "Upload assessment attachment")
    @PostMapping(value = "/{employeeId}/{assessmentId}/attachment")
    public Mono<UploadAssessmentAttachmentResponse> uploadAttachment(@PathVariable("employeeId") int employeeId,
                                                                     @PathVariable int assessmentId,
                                                                     @RequestPart("file") Mono<FilePart> multipartFile,
                                                                     @RequestHeader(value=HttpHeaders.CONTENT_LENGTH, required = true) long contentLength) {
        log.debug("Upload new attachment for assessment {}:{}", employeeId, assessmentId);
        return AuthHandler.currentAuth().flatMap(auth -> multipartFile
                .flatMap(it -> service.uploadAttachment(auth, employeeId, assessmentId, it, contentLength)));
    }

    @Operation(summary = "Delete assessment attachment")
    @DeleteMapping(value = "/{employeeId}/{assessmentId}/attachment/{filename}")
    public Mono<DeleteAssessmentAttachmentResponse> deleteAttachment(@PathVariable("employeeId") int employeeId,
                                                                     @PathVariable int assessmentId,
                                                                     @PathVariable String filename,
                                                                     @RequestHeader(value=HttpHeaders.CONTENT_LENGTH, required = true) long contentLength) {
        log.debug("Upload new attachment for assessment {}:{}. Content length={}", employeeId, assessmentId, contentLength);
        return AuthHandler.currentAuth().flatMap(auth -> service.deleteAttachment(auth, employeeId, assessmentId, filename));
    }

}
