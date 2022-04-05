package ru.abondin.hreasy.platform.service.admin.article;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.config.HrEasyFileStorageProperties;
import ru.abondin.hreasy.platform.repo.article.ArticleEntry;
import ru.abondin.hreasy.platform.repo.article.ArticleHistoryRepo;
import ru.abondin.hreasy.platform.repo.article.ArticleRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.FileStorage;
import ru.abondin.hreasy.platform.service.admin.AdminSecurityValidator;
import ru.abondin.hreasy.platform.service.article.ArticleService;
import ru.abondin.hreasy.platform.service.article.dto.*;

import java.time.OffsetDateTime;

/**
 * Stream markdown articles
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdminArticleService {

    private final FileStorage fileStorage;

    private final ArticleRepo articleRepo;
    private final ArticleHistoryRepo articleHistoryRepo;
    private final ArticleMapper mapper;
    private final DateTimeService dateTimeService;
    private final AdminSecurityValidator securityValidator;
    private final HrEasyFileStorageProperties properties;

    public Flux<ArticleDto> all(AuthContext auth) {
        return securityValidator.validateEditArticle(auth)
                .flatMapMany(sec -> articleRepo.findAll().map(mapper::fromEntry));
    }

    @Transactional
    public Mono<Integer> createArticle(AuthContext auth, CreateArticleBody body) {
        log.info("Creating new article {}:{} by {}", body.getArticleGroup(), body.getName(), auth.getUsername());
        var now = dateTimeService.now();
        return securityValidator.validateEditArticle(auth).flatMap((sec) -> {
            var entry = new ArticleEntry();
            entry.setModerated(body.isModerated());
            entry.setContent(body.getContent());
            entry.setArchived(false);
            entry.setArticleGroup(body.getArticleGroup());
            entry.setName(body.getName());
            entry.setCreatedAt(now);
            entry.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
            entry.setUpdatedAt(now);
            entry.setUpdatedBy(auth.getEmployeeInfo().getEmployeeId());
            return doUpdate(auth, entry, now);
        });
    }

    @Transactional
    public Mono<Integer> updateArticle(AuthContext auth, int articleId, UpdateArticleBody body) {
        log.info("Update article {} by {}", articleId, auth.getUsername());
        var now = dateTimeService.now();
        return securityValidator.validateEditArticle(auth)
                .flatMap(sec -> articleRepo.findById(articleId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(articleId))))
                .flatMap(entry -> {
                    entry.setArticleGroup(body.getArticleGroup());
                    entry.setName(body.getName());
                    entry.setArchived(body.isArchived());
                    entry.setContent(body.getContent());
                    entry.setDescription(body.getDescription());
                    entry.setModerated(body.isModerated());
                    entry.setUpdatedAt(now);
                    entry.setUpdatedBy(auth.getEmployeeInfo().getEmployeeId());
                    return doUpdate(auth, entry, now);
                });
    }

    /**
     * Upload all article attachment to article folder inside file storage
     *
     * @param auth
     * @param articleId
     * @param file
     * @return
     */
    public Mono<UploadArticleAttachmentResponse> uploadArticleAttachment(AuthContext auth, int articleId, FilePart file) {
        return securityValidator.validateEditArticle(auth)
                .flatMap(sec -> {
                    var filename = file.filename();
                    return fileStorage.uploadFile(ArticleService.getArticleAttachmentFolder(articleId), filename, file)
                            .then(Mono.just(new UploadArticleAttachmentResponse(properties.getArticleAttachmentRelativePattern()
                            .replace("{articleId}", Integer.toString(articleId))
                            .replace("{fileName}", filename))));
                });
    }

    private Mono<Integer> doUpdate(AuthContext auth, ArticleEntry entry, OffsetDateTime now) {
        return articleRepo.save(entry).flatMap(persisted -> {
            var history = mapper.history(entry);
            history.setArticleId(persisted.getId());
            history.setCreatedAt(now);
            history.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
            return articleHistoryRepo.save(history).map(h -> persisted.getId());
        });
    }


}
