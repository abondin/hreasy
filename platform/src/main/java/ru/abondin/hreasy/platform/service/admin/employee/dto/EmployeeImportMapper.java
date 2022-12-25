package ru.abondin.hreasy.platform.service.admin.employee.dto;

import io.r2dbc.postgresql.codec.Json;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.r2dbc.repository.Query;
import ru.abondin.hreasy.platform.repo.employee.admin.imp.ImportEmployeesWorkflowEntry;
import ru.abondin.hreasy.platform.service.mapper.MapperBaseWithJsonSupport;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class EmployeeImportMapper extends MapperBaseWithJsonSupport {
    public Json config(EmployeeImportConfig config){
        return config == null ? null : Json.of(toJsonString(config));
    }


    @Mapping(target = "config", qualifiedByName = "config", source = "config")
    @Mapping(target = "data", qualifiedByName = "data", source = "data")
    public abstract ImportEmployeesWorkflowDto fromEntry(ImportEmployeesWorkflowEntry entry);

    @Named("config")
    protected EmployeeImportConfig config(Json config){
        return fromJson(config, EmployeeImportConfig.class);
    }

    @Named("data")
    protected List<ImportEmployeesWorkflowDto> data(Json config){
        return listFromJson(config, ImportEmployeesWorkflowDto.class);
    }
}
