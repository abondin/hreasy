package ru.abondin.hreasy.platform.service.admin.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.abondin.hreasy.platform.repo.dict.DictProjectEntry;

import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")
public interface ProjectDtoMapper {

    DictProjectEntry fromDto(ProjectDto.CreateOrUpdateProjectDto dto);

    DictProjectEntry fromDto(ProjectDto dto);

    @Mapping(source = "departmentId", target = "department.id")
    @Mapping(source = "departmentName", target = "department.name")
    ProjectDto fromEntry(DictProjectEntry.ProjectFullEntry entry);


    default DictProjectEntry.ProjectHistoryEntry historyEntry(int empoyeeId, OffsetDateTime updateTime, DictProjectEntry projectEntry) {
        var result = partionCopyHistory(projectEntry);
        result.setUpdatedAt(updateTime);
        result.setUpdatedBy(empoyeeId);
        return result;
    }

    DictProjectEntry.ProjectHistoryEntry partionCopyHistory(DictProjectEntry projectEntry);

}
