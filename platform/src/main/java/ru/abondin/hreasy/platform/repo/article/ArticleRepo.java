package ru.abondin.hreasy.platform.repo.article;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ArticleRepo extends ReactiveCrudRepository<ArticleEntry, Integer> {

    @Query("select * from article.article where archived!=true and moderated = true order by updated_at")
    Flux<ArticleEntry> moderatedNotArchived();

}
