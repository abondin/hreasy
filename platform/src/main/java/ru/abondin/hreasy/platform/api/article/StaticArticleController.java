package ru.abondin.hreasy.platform.api.article;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.service.article.ArticleService;
import ru.abondin.hreasy.platform.service.article.dto.ArticleMetaDto;

/**
 * Static Markdown articles (news for example)
 */
@RestController()
@RequestMapping("/api/v1/article")
@RequiredArgsConstructor
@Slf4j
public class StaticArticleController {

    private final ArticleService service;

    @Operation(summary = "Get all shared articles metadata")
    @GetMapping(value = "shared")
    public Flux<ArticleMetaDto> shared() {
        return service.sharedArticles();
    }

    @Operation(summary = "Get shared article content")
    @GetMapping(value = "shared/{articleName}", produces = MediaType.TEXT_MARKDOWN_VALUE)
    public Mono<Resource> shared(@PathVariable String articleName) {
        return service.streamSharedArticle(articleName);
    }

}
