package ru.abondin.hreasy.platform.repo.article;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.repo.dict.DepartmentEntry;

@Repository
public interface ArticleRepo extends ReactiveCrudRepository<ArticleEntry, Integer> {

    @Query("select * from article where archived!=1 and moderated = 1 order by updated_at")
    Flux<ArticleEntry> moderatedNotArchived();

}
