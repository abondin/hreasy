package ru.abondin.hreasy.platform.service.assessment;


import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.config.HrEasyFileStorageProperties;
import ru.abondin.hreasy.platform.repo.assessment.AssessmentFormRepo;
import ru.abondin.hreasy.platform.repo.assessment.AssessmentRepo;
import ru.abondin.hreasy.platform.repo.assessment.EmployeeAssessmentEntry;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.FileStorage;
import ru.abondin.hreasy.platform.service.assessment.dto.*;

import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class AssessmentService {

    public static final String ASSESSMENT_BASE_DIR = "assessment";

    public static String getAssessmentAttachmentFolder(int employeeid, int assessmentId) {
        return ASSESSMENT_BASE_DIR + File.separator + employeeid + File.separator + assessmentId;
    }

    private final AssessmentSecurityValidator securityValidator;
    private final AssessmentRepo assessmentRepo;
    private final AssessmentFormRepo formRepo;
    private final DateTimeService dateTimeService;
    private final AssessmentMapper mapper;
    private final FileStorage fileStorage;
    private final HrEasyFileStorageProperties fileProps;
    private final AssessmentAccessTokenProvider tokenProvider;

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

    public Mono<UploadAssessmentAttachmentResponse> uploadAttachment(AuthContext auth, int employeeId, int assessmentId, FilePart file) {
        return validateOwnerOrCanViewAssessmentFull(auth, assessmentId)
                .flatMap(v -> {
                    var filename = file.filename();
                    return fileStorage.uploadFile(getAssessmentAttachmentFolder(employeeId, assessmentId), filename, file)
                            .then(Mono.just(new UploadAssessmentAttachmentResponse()));
                });
    }


    /**
     * Get detailed assessment information includes all forms and attachments
     *
     * @param auth
     * @param assessmentId
     * @return
     */
    public Mono<AssessmentWithFormsAndFiles> getAssessment(AuthContext auth, int employeeId, int assessmentId) {
        return validateOwnerOrCanViewAssessmentFull(auth, assessmentId)
                // 1. Get assessment entry with base info
                .flatMap(v -> assessmentRepo.findById(assessmentId))
                .flatMap(assessmentEntry -> {
                    // 2. Generate access token to download attachments
                    var accessToken = tokenProvider.generateToken(employeeId, assessmentId, auth.getEmployeeInfo().getEmployeeId());
                    // 3. List file storage to get all attachments filenames
                    return fileStorage.listFiles(getAssessmentAttachmentFolder(employeeId, assessmentId), true)
                            .collectList().flatMap(
                                    //4. Get all assessment forms
                                    files -> formRepo.findByAssessmentId(assessmentId).collectList()
                                            // 5. Combine all together
                                            .map(forms -> mapper.assessmentWithFormsAndFiles(assessmentEntry, forms, files, accessToken))
                            );

                });
    }

    public Mono<Boolean> validateOwnerOrCanViewAssessmentFull(AuthContext auth, int assessmentId) {
        return assessmentRepo.findAllAssessmentOwners(assessmentId)
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(assessmentId))))
                .collectList()
                .flatMap(assessmentOwners -> securityValidator.validateOwnerOrCanViewAssessmentFull(auth, assessmentOwners));
    }

    public Mono<? extends DeleteAssessmentAttachmentResponse> deleteAttachment(AuthContext auth, int employeeId, int assessmentId, String filename) {
        return validateOwnerOrCanViewAssessmentFull(auth, assessmentId)
                .flatMap(v -> fileStorage.toRecycleBin(getAssessmentAttachmentFolder(employeeId, assessmentId), filename))
                .map(deleted -> new DeleteAssessmentAttachmentResponse(deleted));
    }
}


