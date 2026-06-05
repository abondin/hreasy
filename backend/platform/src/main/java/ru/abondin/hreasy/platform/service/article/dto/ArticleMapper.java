package ru.abondin.hreasy.platform.service.article.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.abondin.hreasy.platform.repo.article.ArticleEntry;
import ru.abondin.hreasy.platform.repo.article.ArticleHistoryEntry;

@Mapper(componentModel = "spring")
public interface ArticleMapper {

    ArticleDto fromEntry(ArticleEntry entry);

    @Mapping(source = "id", target = "articleId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    ArticleHistoryEntry history(ArticleEntry entry);

    ArticleEntry fromCreateOrUpdateBody(UpdateArticleBody body);
}
