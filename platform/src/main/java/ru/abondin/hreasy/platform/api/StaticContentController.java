package ru.abondin.hreasy.platform.api;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.sec.SecurityUtils;
import ru.abondin.hreasy.platform.service.FileStorage;
import ru.abondin.hreasy.platform.service.article.ArticleService;

@RestController()
@RequestMapping("/api/v1/fs")
@RequiredArgsConstructor
@Slf4j
public class StaticContentController {

    private final FileStorage fileStorage;

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

    @Operation(summary = "Upload employee avatar")
    @PostMapping(value = "avatar/{employeeId}/upload")
    public Mono<String> uploadAvatar(@PathVariable int employeeId, @RequestPart("file") Flux<FilePart> multipartFile) {
        log.debug("Upload new avatar for " + employeeId);
        return AuthHandler.currentAuth().flatMap(auth -> {
            SecurityUtils.validateUploadAvatar(auth, employeeId);
            return multipartFile
                    .flatMap(it -> fileStorage.uploadFile("avatars", employeeId + ".png", it))
                    .then(Mono.just("OK"));
        });
    }

}
