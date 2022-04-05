package ru.abondin.hreasy.platform.repo.article;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@Table("article.article_history")
public class ArticleHistoryEntry {
    @Id
    private Integer id;
    private Integer articleId;
    private String name;
    private String articleGroup;
    private String description;
    private String content;
    private boolean moderated = true;
    private boolean archived = false;
    private OffsetDateTime createdAt;
    private Integer createdBy;
}
