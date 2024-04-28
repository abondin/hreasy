package ru.abondin.hreasy.platform.service.admin.imp.dto;

import io.r2dbc.postgresql.codec.Json;
import ru.abondin.hreasy.platform.repo.employee.admin.imp.ImportWorkflowEntry;
import ru.abondin.hreasy.platform.service.mapper.MapperBaseWithJsonSupport;

import java.util.List;

public abstract class ExcelImportMapperBase<C extends ExcelImportConfig, R extends ExcelImportRowDto> extends MapperBaseWithJsonSupport {
    public Json impCfg(C config) {
        return config == null ? null : Json.of(toJsonStr(config, true));
    }

    public Json importedRowsToJson(List<R> data) {
        return data == null ? null : Json.of(toJsonStr(data, true));
    }

    public Json stats(ExcelImportProcessStats stats) {
        return stats == null ? null : Json.of(toJsonStr(stats, true));
    }

    public abstract <C extends ExcelImportConfig, R extends ExcelImportRowDto> ExcelImportWorkflowDto<C, R> fromEntry(ImportWorkflowEntry entry);

    public abstract List<R> importedRowsFromJson(Json data);
}
