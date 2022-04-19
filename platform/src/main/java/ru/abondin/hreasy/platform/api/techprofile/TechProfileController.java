package ru.abondin.hreasy.platform.api.techprofile;


import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.techprofile.TechProfileService;
import ru.abondin.hreasy.platform.service.techprofile.dto.EmployeeTechProfileFileDto;
import ru.abondin.hreasy.platform.service.techprofile.dto.TechProfileDeletedResponse;
import ru.abondin.hreasy.platform.service.techprofile.dto.UploadTechprofileResponse;

/**
 * View my
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/techprofile")
public class TechProfileController {

    private final TechProfileService service;

    @GetMapping("/{employeeId}")
    public Flux<EmployeeTechProfileFileDto> techProfile(@PathVariable("employeeId") int employeeId) {
        return AuthHandler.currentAuth().flatMapMany(auth -> service.getTechProfiles(auth, employeeId));
    }

    @Operation(summary = "Upload tech profile document")
    @PostMapping(value = "/{employeeId}/file")
    public Mono<UploadTechprofileResponse> upload(@PathVariable("employeeId") int employeeId,
                                                  @RequestPart("file") Mono<FilePart> multipartFile,
                                                  @RequestHeader(HttpHeaders.CONTENT_LENGTH) long contentLength) {
        return AuthHandler.currentAuth().flatMap(auth -> multipartFile
                .flatMap(it -> service.upload(auth, employeeId, it, contentLength)));
    }

    @Operation(summary = "Delete tech profile document")
    @DeleteMapping(value = "/{employeeId}/file/{filename}")
    public Mono<TechProfileDeletedResponse> upload(@PathVariable("employeeId") int employeeId,
                                                   @PathVariable("filename") String filename) {
        return AuthHandler.currentAuth().flatMap(auth -> service
                .delete(auth, employeeId, filename));
    }
}
