package ru.abondin.hreasy.platform.service.admin.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.abondin.hreasy.platform.repo.dict.DictProjectEntry;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")
public interface ProjectDtoMapper {

    DictProjectEntry fromDto(ProjectDto.CreateOrUpdateProjectDto dto);

    DictProjectEntry fromDto(ProjectDto dto);

    @Mapping(target = "department", qualifiedByName = "department", source = ".")
    ProjectDto fromEntry(DictProjectEntry.ProjectFullEntry entry);


    default DictProjectEntry.ProjectHistoryEntry historyEntry(int empoyeeId, OffsetDateTime updateTime, DictProjectEntry projectEntry) {
        var result = partionCopyHistory(projectEntry);
        result.setUpdatedAt(updateTime);
        result.setUpdatedBy(empoyeeId);
        return result;
    }

    @Mapping(source = "id", target = "projectId")
    DictProjectEntry.ProjectHistoryEntry partionCopyHistory(DictProjectEntry projectEntry);


    default SimpleDictDto department(DictProjectEntry.ProjectFullEntry entry) {
        return simpleDto(entry.getDepartmentId(), entry.getDepartmentName());
    }

    default SimpleDictDto simpleDto(Integer id, String name) {
        return id == null ? null : new SimpleDictDto(id, name);
    }
}
