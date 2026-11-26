package ru.abondin.hreasy.platform.service.article.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import org.springframework.lang.NonNull;

/**
 * Information about article
 */
@Data
@NoArgsConstructor
public class UpdateArticleBody {
    @NonNull
    private String name;
    @NonNull
    private String articleGroup;
    @Nullable
    private String description;
    @Nullable
    private String content;
    private boolean moderated = true;
    private boolean archived = false;
}
