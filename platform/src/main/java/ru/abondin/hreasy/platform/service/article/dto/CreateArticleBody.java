package ru.abondin.hreasy.platform.service.article.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;

/**
 * Create empty article
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateArticleBody {
    @NotNull
    private String name;
    @NotNull
    private String articleGroup;
    @Nullable
    private String description;
}
