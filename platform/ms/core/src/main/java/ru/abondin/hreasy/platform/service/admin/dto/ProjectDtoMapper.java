package ru.abondin.hreasy.platform.service.admin.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.dict.DictProjectEntry;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;

import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")
public interface ProjectDtoMapper extends MapperBase {

    DictProjectEntry fromDto(ProjectDto.CreateOrUpdateProjectDto dto);

    @Mapping(target = "department", qualifiedByName = "department", source = ".")
    @Mapping(target = "businessAccount", qualifiedByName = "businessAccount", source = ".")
    ProjectDto fromEntry(DictProjectEntry.ProjectFullEntry entry);


    default DictProjectEntry.ProjectHistoryEntry historyEntry(int employeeId, OffsetDateTime updateTime, DictProjectEntry projectEntry) {
        var result = partialCopyHistory(projectEntry);
        result.setUpdatedAt(updateTime);
        result.setUpdatedBy(employeeId);
        return result;
    }

    @Mapping(source = "id", target = "projectId")
    @Mapping(target = "id", ignore = true)
    DictProjectEntry.ProjectHistoryEntry partialCopyHistory(DictProjectEntry projectEntry);


    @Named("department")
    default SimpleDictDto department(DictProjectEntry.ProjectFullEntry entry) {
        return simpleDto(entry.getDepartmentId(), entry.getDepartmentName());
    }


    @Named("businessAccount")
    default SimpleDictDto businessAccount(DictProjectEntry.ProjectFullEntry entry) {
        return simpleDto(entry.getBaId(), entry.getBaName());
    }

}
