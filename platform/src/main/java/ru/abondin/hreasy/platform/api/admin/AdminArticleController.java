package ru.abondin.hreasy.platform.api.admin;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.admin.article.AdminArticleService;
import ru.abondin.hreasy.platform.service.article.dto.ArticleDto;
import ru.abondin.hreasy.platform.service.article.dto.CreateArticleBody;
import ru.abondin.hreasy.platform.service.article.dto.UpdateArticleBody;

/**
 * Static Markdown articles (news for example)
 */
@RestController()
@RequestMapping("/api/v1/admin/article")
@RequiredArgsConstructor
@Slf4j
public class AdminArticleController {

    private final AdminArticleService service;

    @Operation(summary = "Get all articles with content. Include archived and moderated")
    @GetMapping(value = "")
    public Flux<ArticleDto> allArticles() {
        return AuthHandler.currentAuth().flatMapMany(auth -> service.all(auth));
    }

    @PostMapping("")
    public Mono<Integer> createArticle(CreateArticleBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> service.createArticle(auth, body));
    }

    @PutMapping("{articleId}")
    public Mono<Integer> createArticle(int articleId, UpdateArticleBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> service.updateArticle(auth, articleId, body));
    }

    @Operation(summary = "Upload article attachment")
    @PostMapping(value = "{articleId}/attachment/{attachmentName}")
    public Mono<String> uploadAvatar(@PathVariable int articleId,
                                     @PathVariable String attachmentName,
                                     @RequestPart("file") Flux<FilePart> multipartFile) {
        log.debug("Upload new attachment {} for article {}", attachmentName, articleId);
        return AuthHandler.currentAuth().flatMap(auth -> multipartFile
                .flatMap(it -> service.uploadArticleAttachment(auth, articleId, attachmentName, it))
                .then(Mono.just("OK")));
    }
}
