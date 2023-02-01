package ru.abondin.hreasy.platform.service.ts.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.ts.TimesheetSummaryView;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;

@Mapper(componentModel = "spring")
public interface TimesheetMapper extends MapperBase {

    @Mapping(source = ".", target = "employee", qualifiedByName = "employee")
    TimesheetSummaryDto fromEntry(TimesheetSummaryView entry);


    @Named("employee")
    default SimpleDictDto employee(TimesheetSummaryView entry) {
        return simpleDto(entry.getEmployee(), entry.getEmployeeDisplayname());
    }

}
