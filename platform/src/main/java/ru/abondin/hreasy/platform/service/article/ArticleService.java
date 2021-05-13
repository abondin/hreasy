package ru.abondin.hreasy.platform.service.article;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.service.FileStorage;
import ru.abondin.hreasy.platform.service.article.dto.ArticleMetaDto;

import java.io.File;

/**
 * Stream markdown articles
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleService {
    private final String ARTICLE_BASE_DIR = "article";
    private final String ARTICLE_SHARED = "shared";
    private final String ARTICLE_RESOURCES = "res";

    private final FileStorage fileStorage;


    public Flux<ArticleMetaDto> sharedArticles() {
        return fileStorage.listFiles(
                concatPath(ARTICLE_BASE_DIR, ARTICLE_SHARED), true)
                .map(fileName -> new ArticleMetaDto(fileName));
    }

    public Mono<Resource> streamSharedArticle(String articleName) {
        return fileStorage.streamFile(
                concatPath(ARTICLE_BASE_DIR, ARTICLE_SHARED), articleName);
    }

    private String concatPath(String... pathItems) {
        String path = "";
        for (var item : pathItems) {
            if (path.length() > 0) {
                path += File.separator;
            }
            path += item;
        }
        return path;
    }
}
