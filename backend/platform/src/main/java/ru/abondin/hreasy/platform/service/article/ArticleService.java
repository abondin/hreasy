package ru.abondin.hreasy.platform.service.article;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.repo.article.ArticleRepo;
import ru.abondin.hreasy.platform.service.article.dto.ArticleDto;
import ru.abondin.hreasy.platform.service.article.dto.ArticleMapper;

import java.io.File;

/**
 * Stream markdown articles
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleService {
    public static final String ARTICLE_BASE_DIR = "article";

    public static String getArticleAttachmentFolder(int articleId) {
        return ARTICLE_BASE_DIR + File.separator + articleId;
    }

    private final ArticleRepo articleRepo;
    private final ArticleMapper mapper;


    /**
     * @return all articles ready to publish (moderated and not archived)
     */
    public Flux<ArticleDto> publishedArticles() {
        return articleRepo.moderatedNotArchived().map(mapper::fromEntry);
    }

}
