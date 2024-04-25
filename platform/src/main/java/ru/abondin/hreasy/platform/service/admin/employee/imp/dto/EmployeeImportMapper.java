package ru.abondin.hreasy.platform.service.admin.employee.imp.dto;

import io.r2dbc.postgresql.codec.Json;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.employee.admin.imp.ImportEmployeesWorkflowEntry;
import ru.abondin.hreasy.platform.service.admin.imp.ExcelImportProcessStats;
import ru.abondin.hreasy.platform.service.admin.imp.ExcelImportWorkflowDto;
import ru.abondin.hreasy.platform.service.mapper.MapperBaseWithJsonSupport;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class EmployeeImportMapper extends MapperBaseWithJsonSupport {
    public Json impCfg(EmployeeImportConfig config) {
        return config == null ? null : Json.of(toJsonStr(config, true));
    }

    public Json importedRows(List<ImportEmployeeExcelRowDto> data) {
        return data == null ? null : Json.of(toJsonStr(data, true));
    }

    public Json stats(ExcelImportProcessStats stats) {
        return stats == null ? null : Json.of(toJsonStr(stats, true));
    }

    @Mapping(target = "config", qualifiedByName = "importEmployeeConfig", source = "config")
    @Mapping(target = "importedRows", qualifiedByName = "importedRows", source = "importedRows")
    @Mapping(target = "importProcessStats", qualifiedByName = "importProcessStats", source = "importProcessStats")
    public abstract ExcelImportWorkflowDto<EmployeeImportConfig, ImportEmployeeExcelRowDto> fromEntry(ImportEmployeesWorkflowEntry entry);

    @Named("importEmployeeConfig")
    protected EmployeeImportConfig importEmployeeConfig(Json config) {
        return fromJson(config, EmployeeImportConfig.class);
    }

    @Named("importedRows")
    public List<ImportEmployeeExcelRowDto> importedRows(Json data) {
        return listFromJson(data, ImportEmployeeExcelRowDto.class);
    }

    @Named("importProcessStats")
    protected ExcelImportProcessStats importProcessStats(Json data) {
        return fromJson(data, ExcelImportProcessStats.class);
    }
}
