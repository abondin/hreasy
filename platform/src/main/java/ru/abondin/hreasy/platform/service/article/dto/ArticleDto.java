package ru.abondin.hreasy.platform.service.article.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Information about article
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDto {
    private int id;
    private String name;
    private String articleGroup;
    private String description;
    private String content;
    private boolean moderated = true;
    private boolean archived = false;
    private OffsetDateTime updatedAt;
}
