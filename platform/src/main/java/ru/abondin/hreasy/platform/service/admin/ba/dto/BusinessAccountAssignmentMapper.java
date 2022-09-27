package ru.abondin.hreasy.platform.service.admin.ba.dto;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountAssignmentEntry;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountAssignmentView;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")

public interface BusinessAccountAssignmentMapper extends MapperBase {


    @Mapping(target = "businessAccount", qualifiedByName = "businessAccount", source = ".")
    @Mapping(target = "employee", qualifiedByName = "employee", source = ".")
    @Mapping(target = "project", qualifiedByName = "project", source = ".")
    @Mapping(target = "closedBy", qualifiedByName = "closedBy", source = ".")
    BusinessAccountAssignmentDto fromEntry(BusinessAccountAssignmentView entry);


    BusinessAccountAssignmentEntry toEntry(@NotNull CreateBusinessAccountAssignmentBody body,
                                           int businessAccount, int period,
                                           Integer createdBy, OffsetDateTime createdAt);

    @Named("businessAccount")
    default SimpleDictDto businessAccount(BusinessAccountAssignmentView entry) {
        return simpleDto(entry.getBusinessAccount(), entry.getBusinessAccountName());
    }

    @Named("employee")
    default SimpleDictDto employee(BusinessAccountAssignmentView entry) {
        return simpleDto(entry.getEmployee(), entry.getEmployeeDisplayName());
    }

    @Named("project")
    default SimpleDictDto project(BusinessAccountAssignmentView entry) {
        return simpleDto(entry.getProject(), entry.getProjectName());
    }

    @Named("closedBy")
    default SimpleDictDto closedBy(BusinessAccountAssignmentView entry) {
        return simpleDto(entry.getClosedBy(), entry.getClosedByDisplayName());
    }


}
