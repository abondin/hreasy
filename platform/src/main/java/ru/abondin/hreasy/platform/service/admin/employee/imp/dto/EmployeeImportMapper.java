package ru.abondin.hreasy.platform.service.admin.employee.imp.dto;

import io.r2dbc.postgresql.codec.Json;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.employee.admin.imp.ImportWorkflowEntry;
import ru.abondin.hreasy.platform.service.admin.imp.dto.*;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class EmployeeImportMapper extends ExcelImportMapperBase<EmployeeImportConfig, ImportEmployeeExcelRowDto> {

    @Mapping(target = "config", qualifiedByName = "importEmployeeConfig", source = "config")
    @Mapping(target = "importedRows", qualifiedByName = "importedRowsFromJson", source = "importedRows")
    @Mapping(target = "importProcessStats", qualifiedByName = "importProcessStats", source = "importProcessStats")
    @Override
    public abstract ExcelImportWorkflowDto<EmployeeImportConfig, ImportEmployeeExcelRowDto> fromEntry(ImportWorkflowEntry entry);

    @Named("importEmployeeConfig")
    protected EmployeeImportConfig importEmployeeConfig(Json config) {
        return fromJson(config, EmployeeImportConfig.class);
    }

    @Named("importedRowsFromJson")
    @Override
    public List<ImportEmployeeExcelRowDto> importedRowsFromJson(Json data) {
        return listFromJson(data, ImportEmployeeExcelRowDto.class);
    }

    @Named("importProcessStats")
    protected ExcelImportProcessStats importProcessStats(Json data) {
        return fromJson(data, ExcelImportProcessStats.class);
    }
}
