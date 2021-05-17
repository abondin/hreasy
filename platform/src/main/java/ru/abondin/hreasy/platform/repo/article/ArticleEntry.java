package ru.abondin.hreasy.platform.repo.article;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@Table("article")
public class ArticleEntry {
    @Id
    private Integer id;
    private String name;
    private String articleGroup;
    private String description;
    private String content;
    private boolean moderated = true;
    private boolean archived = false;
    private OffsetDateTime createdAt;
    private Integer createdBy;
    private OffsetDateTime updatedAt;
    private Integer updatedBy;
}
