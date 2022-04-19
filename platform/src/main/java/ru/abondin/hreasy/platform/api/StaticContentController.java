package ru.abondin.hreasy.platform.api;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.sec.SecurityUtils;
import ru.abondin.hreasy.platform.service.FileStorage;
import ru.abondin.hreasy.platform.service.article.ArticleService;
import ru.abondin.hreasy.platform.service.assessment.AssessmentAccessTokenProvider;
import ru.abondin.hreasy.platform.service.assessment.AssessmentService;
import ru.abondin.hreasy.platform.service.techprofile.TechProfileAccessTokenProvider;
import ru.abondin.hreasy.platform.service.techprofile.TechProfileService;

@RestController()
@RequestMapping("/api/v1/fs")
@RequiredArgsConstructor
@Slf4j
public class StaticContentController {

    private final FileStorage fileStorage;
    private final AssessmentAccessTokenProvider assessmentAccessTokenProvider;
    private final TechProfileAccessTokenProvider techProfileAccessTokenProvider;

    @Operation(summary = "Get employee avatar")
    @GetMapping(value = "avatar/{employeeId}", produces = MediaType.IMAGE_PNG_VALUE)
    public Mono<Resource> avatar(@PathVariable int employeeId) {
        return fileStorage.streamImage("avatars", employeeId + ".png", true);
    }

    @Operation(summary = "Get article static attachment")
    @GetMapping(value = "article/{articleId}/{attachmentName}")
    public Mono<Resource> articleAttachment(@PathVariable int articleId, @PathVariable String attachmentName) {
        return fileStorage.streamFile(ArticleService.getArticleAttachmentFolder(articleId), attachmentName);
    }

    @Operation(summary = "Get assessment static attachment")
    @GetMapping(value = "assessment/{employeeId}/{assessmentId}/{attachmentName}/{accessToken}")
    public Mono<Resource> assessmentAttachment(@PathVariable int employeeId,
                                               @PathVariable int assessmentId,
                                               @PathVariable String attachmentName,
                                               @PathVariable String accessToken) {
        return assessmentAccessTokenProvider.validateToken(AssessmentAccessTokenProvider.AssessmentAccessTokenContent.builder()
                .assessmentEmployeeId(employeeId)
                .assessmentId(assessmentId).build(), accessToken).flatMap(v ->
                fileStorage.streamFile(AssessmentService.getAssessmentAttachmentFolder(employeeId, assessmentId), attachmentName));
    }


    @Operation(summary = "Upload employee avatar")
    @PostMapping(value = "avatar/{employeeId}/upload")
    public Mono<String> uploadAvatar(@PathVariable int employeeId,
                                     @RequestPart("file") Flux<FilePart> multipartFile
            , @RequestHeader(value = HttpHeaders.CONTENT_LENGTH, required = true) long contentLength) {
        log.debug("Upload new avatar for " + employeeId);
        return AuthHandler.currentAuth().flatMap(auth -> {
            SecurityUtils.validateUploadAvatar(auth, employeeId);
            return multipartFile
                    .flatMap(it -> fileStorage.uploadFile("avatars", employeeId + ".png", it, contentLength))
                    .then(Mono.just("OK"));
        });
    }

    @Operation(summary = "Get tech profile file")
    @GetMapping(value = "techprofile/{employeeId}/{accessToken}/{filename}")
    public Mono<Resource> techProfile(@PathVariable int employeeId,
                                      @PathVariable String accessToken,
                                      @PathVariable String filename) {
        return techProfileAccessTokenProvider.validateToken(TechProfileAccessTokenProvider.TechProfileTokenContent.builder()
                .techProfileEmployeeId(employeeId)
                .build(), accessToken).flatMap(v ->
                fileStorage.streamFile(TechProfileService.getTechProfileFolder(employeeId), filename));
    }

}
