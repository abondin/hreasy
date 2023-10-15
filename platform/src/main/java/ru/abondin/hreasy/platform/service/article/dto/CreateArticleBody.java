package ru.abondin.hreasy.platform.service.article.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import org.springframework.lang.NonNull;

/**
 * Create empty article
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateArticleBody {
    @NonNull
    private String name;
    @NonNull
    private String articleGroup;
    @Nullable
    private String description;
    @Nullable
    private String content;
    private boolean moderated = true;

}
