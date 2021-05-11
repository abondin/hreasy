package ru.abondin.hreasy.platform.service.admin.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.abondin.hreasy.platform.repo.dict.DictProjectEntry;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;

import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")
public interface ProjectDtoMapper extends MapperBase {

    DictProjectEntry fromDto(ProjectDto.CreateOrUpdateProjectDto dto);

    DictProjectEntry fromDto(ProjectDto dto);

    @Mapping(target = "department", qualifiedByName = "department", source = ".")
    ProjectDto fromEntry(DictProjectEntry.ProjectFullEntry entry);


    default DictProjectEntry.ProjectHistoryEntry historyEntry(int employeeId, OffsetDateTime updateTime, DictProjectEntry projectEntry) {
        var result = partialCopyHistory(projectEntry);
        result.setUpdatedAt(updateTime);
        result.setUpdatedBy(employeeId);
        return result;
    }

    @Mapping(source = "id", target = "projectId")
    DictProjectEntry.ProjectHistoryEntry partialCopyHistory(DictProjectEntry projectEntry);


    default SimpleDictDto department(DictProjectEntry.ProjectFullEntry entry) {
        return simpleDto(entry.getDepartmentId(), entry.getDepartmentName());
    }

}
