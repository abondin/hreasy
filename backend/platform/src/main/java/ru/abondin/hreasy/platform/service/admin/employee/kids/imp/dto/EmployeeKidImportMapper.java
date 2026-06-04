package ru.abondin.hreasy.platform.service.admin.employee.kids.imp.dto;

import io.r2dbc.postgresql.codec.Json;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.employee.admin.imp.ImportWorkflowEntry;
import ru.abondin.hreasy.platform.service.admin.imp.dto.ExcelImportMapperBase;
import ru.abondin.hreasy.platform.service.admin.imp.dto.ExcelImportProcessStats;
import ru.abondin.hreasy.platform.service.admin.imp.dto.ExcelImportWorkflowDto;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class EmployeeKidImportMapper extends ExcelImportMapperBase<EmployeeKidImportConfig, ImportEmployeeKidExcelRowDto> {

    @Mapping(target = "config", qualifiedByName = "importEmployeeConfig", source = "config")
    @Mapping(target = "importedRows", qualifiedByName = "importedRowsFromJson", source = "importedRows")
    @Mapping(target = "importProcessStats", qualifiedByName = "importProcessStats", source = "importProcessStats")
    @Override
    public abstract ExcelImportWorkflowDto<EmployeeKidImportConfig, ImportEmployeeKidExcelRowDto> fromEntry(ImportWorkflowEntry entry);

    @Named("importEmployeeConfig")
    protected EmployeeKidImportConfig importEmployeeKidConfig(Json config) {
        return fromJson(config, EmployeeKidImportConfig.class);
    }

    @Named("importedRowsFromJson")
    @Override
    public List<ImportEmployeeKidExcelRowDto> importedRowsFromJson(Json data) {
        return listFromJson(data, ImportEmployeeKidExcelRowDto.class);
    }

    @Named("importProcessStats")
    protected ExcelImportProcessStats importProcessStats(Json data) {
        return fromJson(data, ExcelImportProcessStats.class);
    }
}
