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
import ru.abondin.hreasy.platform.service.assessment.dto.AssessmentDto;
import ru.abondin.hreasy.platform.service.assessment.dto.AssessmentWithFormsAndFiles;
import ru.abondin.hreasy.platform.service.assessment.dto.EmployeeAssessmentsSummary;
import ru.abondin.hreasy.platform.service.assessment.dto.UploadAssessmentAttachmentResponse;

import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class AssessmentService {

    public static final String ASSESSMENT_BASE_DIR = "assessment";

    public static String getAssessmentAttachmentFolder(int assessmentId) {
        return ASSESSMENT_BASE_DIR + File.separator + assessmentId;
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

    public Mono<UploadAssessmentAttachmentResponse> uploadAttachment(AuthContext auth, int assessmentId, FilePart file) {
        return validateOwnerOrCanViewAssessmentFull(auth, assessmentId)
                .flatMap(v -> {
                    var filename = file.filename();
                    return fileStorage.uploadFile(getAssessmentAttachmentFolder(assessmentId), filename, file)
                            .then(Mono.just(new UploadAssessmentAttachmentResponse(fileProps.getAssessmentAttachmentRelativePattern()
                                    .replace("{assessmentId}", Integer.toString(assessmentId))
                                    .replace("{fileName}", filename))));
                });
    }


    /**
     * Get detailed assessment information includes all forms and attachments
     *
     * @param auth
     * @param assessmentId
     * @return
     */
    public Mono<AssessmentWithFormsAndFiles> getAssessment(AuthContext auth, int assessmentId) {
        return validateOwnerOrCanViewAssessmentFull(auth, assessmentId)
                .flatMap(v -> assessmentRepo.findById(assessmentId))
                .flatMap(assessmentEntry -> {
                    var accessToken = tokenProvider.generateToken(assessmentId, auth.getEmployeeInfo().getEmployeeId());
                    return formRepo.findByAssessmentId(assessmentId).collectList()
                            .map(forms -> mapper.assessmentWithFormsAndFiles(assessmentEntry, forms, accessToken));
                });
    }

    public Mono<Boolean> validateOwnerOrCanViewAssessmentFull(AuthContext auth, int assessmentId) {
        return assessmentRepo.findAllAssessmentOwners(assessmentId)
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(assessmentId))))
                .collectList()
                .flatMap(assessmentOwners -> securityValidator.validateOwnerOrCanViewAssessmentFull(auth, assessmentOwners));
    }
}


