package ru.abondin.hreasy.platform.service.admin.employee.imp.dto;

import io.r2dbc.postgresql.codec.Json;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.employee.admin.imp.ImportEmployeesWorkflowEntry;
import ru.abondin.hreasy.platform.service.mapper.MapperBaseWithJsonSupport;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class EmployeeImportMapper extends MapperBaseWithJsonSupport {
    public Json config(EmployeeImportConfig config) {
        return config == null ? null : Json.of(toJsonString(config));
    }

    public Json data(List<ImportEmployeeExcelDto> data) {
        return data == null ? null : Json.of(toJsonString(data));
    }

    public Json stats(ImportProcessStats stats) {
        return stats == null ? null : Json.of(toJsonString(stats));
    }

    @Mapping(target = "config", qualifiedByName = "importEmployeeConfig", source = "config")
    @Mapping(target = "importedRows", qualifiedByName = "importedRows", source = "importedRows")
    @Mapping(target = "importProcessStats", qualifiedByName = "importProcessStats", source = "importProcessStats")
    public abstract ImportEmployeesWorkflowDto fromEntry(ImportEmployeesWorkflowEntry entry);

    @Named("importEmployeeConfig")
    protected EmployeeImportConfig importEmployeeConfig(Json config) {
        return fromJson(config, EmployeeImportConfig.class);
    }

    @Named("importedRows")
    protected List<ImportEmployeeExcelDto> importedRows(Json data) {
        return listFromJson(data, ImportEmployeeExcelDto.class);
    }

    @Named("importProcessStats")
    protected ImportProcessStats importProcessStats(Json data) {
        return fromJson(data, ImportProcessStats.class);
    }
}
