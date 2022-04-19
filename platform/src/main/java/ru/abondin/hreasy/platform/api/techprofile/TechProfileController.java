package ru.abondin.hreasy.platform.api.techprofile;


import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.techprofile.TechProfileService;
import ru.abondin.hreasy.platform.service.techprofile.dto.EmployeeTechProfileFileDto;
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
    @PostMapping(value = "/file")
    public Mono<UploadTechprofileResponse> upload(@RequestPart("file") Mono<FilePart> multipartFile) {
        return AuthHandler.currentAuth().flatMap(auth -> multipartFile
                .flatMap(it -> service.upload(auth, auth.getEmployeeInfo().getEmployeeId(), it)));
    }

    @Operation(summary = "Upload tech profile document")
    @PostMapping(value = "/{employeeId}/file")
    public Mono<UploadTechprofileResponse> upload(@PathVariable("employeeId") int employeeId,
                                                  @RequestPart("file") Mono<FilePart> multipartFile) {
        return AuthHandler.currentAuth().flatMap(auth -> multipartFile
                .flatMap(it -> service.upload(auth, employeeId, it)));
    }


}
