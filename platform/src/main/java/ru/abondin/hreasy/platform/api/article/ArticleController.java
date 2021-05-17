package ru.abondin.hreasy.platform.api.article;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.service.article.ArticleService;
import ru.abondin.hreasy.platform.service.article.dto.ArticleDto;

/**
 * Static Markdown articles (news for example)
 */
@RestController()
@RequestMapping("/api/v1/article")
@RequiredArgsConstructor
@Slf4j
public class ArticleController {

    private final ArticleService service;

    @Operation(summary = "Get all articles with content. Exclude archived and moderated")
    @GetMapping(value = "")
    public Flux<ArticleDto> allArticles() {
        return service.publishedArticles();
    }

}
