package ru.abondin.hreasy.platform.service.techprofile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.techprofile.TechprofileLogEntry;
import ru.abondin.hreasy.platform.repo.techprofile.TechprofileLogRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.FileStorage;
import ru.abondin.hreasy.platform.service.techprofile.dto.EmployeeTechProfileFileDto;
import ru.abondin.hreasy.platform.service.techprofile.dto.TechProfileDeletedResponse;
import ru.abondin.hreasy.platform.service.techprofile.dto.UploadTechprofileResponse;

import java.io.File;

/**
 * Service to upload and download employee tech profile files
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TechProfileService {

    public static final String TECHPROFILE_BASE_DIR = "techprofile";

    public static String getTechProfileFolder(int employeeId) {
        return TECHPROFILE_BASE_DIR + File.separator + employeeId;
    }

    private final DateTimeService dateTimeService;
    private final TechProfileSecurityValidator securityValidator;
    private final FileStorage fileStorage;
    private final TechProfileAccessTokenProvider tokenProvider;
    private final TechprofileLogRepo repo;


    public Flux<EmployeeTechProfileFileDto> getTechProfiles(AuthContext auth, int employeeId) {
        return securityValidator.validateDownloadTechProfile(auth, employeeId)
                .map(valid -> tokenProvider.generateToken(TechProfileAccessTokenProvider.TechProfileTokenContent.builder()
                        .techProfileEmployeeId(employeeId)
                        .build(), employeeId
                ))
                .flatMapMany(token -> fileStorage.listFiles(getTechProfileFolder(employeeId), true)
                        .map(filename -> new EmployeeTechProfileFileDto(filename, token)));
    }

    public Mono<UploadTechprofileResponse> upload(AuthContext auth, int employeeId, FilePart file, long contentLength) {
        var filename = file.filename();
        log.info("Upload new tech profile {} for: {}, by: {}. Content Length=", filename, employeeId, auth.getUsername(), contentLength);
        var logEntry = new TechprofileLogEntry();
        logEntry.setContentLength(contentLength);
        logEntry.setCreatedAt(dateTimeService.now());
        logEntry.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
        logEntry.setFilename(filename);
        logEntry.setEmployee(employeeId);
        return securityValidator.validateUploadOrDeleteTechProfile(auth, employeeId)
                .then(fileStorage.uploadFile(getTechProfileFolder(employeeId), filename, file, contentLength))
                .then(repo.save(logEntry))
                .then(Mono.just(new UploadTechprofileResponse()));
    }

    public Mono<TechProfileDeletedResponse> delete(AuthContext auth, int employeeId, String filename) {
        log.info("Delete tech profile {} for employee {} by {}", filename, employeeId, filename);
        var now = dateTimeService.now();
        return securityValidator.validateUploadOrDeleteTechProfile(auth, employeeId)
                .then(repo.markAsDeleted(employeeId, filename, now, auth.getEmployeeInfo().getEmployeeId()))
                .then(fileStorage.toRecycleBin(getTechProfileFolder(employeeId), filename))
                .then(Mono.just(new TechProfileDeletedResponse()));
    }
}
