package ru.abondin.hreasy.platform.repo.article;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleHistoryRepo extends ReactiveCrudRepository<ArticleHistoryEntry, Integer> {
}
