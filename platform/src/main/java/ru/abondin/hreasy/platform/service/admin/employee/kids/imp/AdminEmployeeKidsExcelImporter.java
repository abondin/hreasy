package ru.abondin.hreasy.platform.service.admin.employee.kids.imp;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jxls.reader.SimpleBlockReaderImpl;
import org.springframework.stereotype.Component;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.service.admin.employee.kids.imp.dto.EmployeeKidImportConfig;
import ru.abondin.hreasy.platform.service.admin.employee.kids.imp.dto.ImportEmployeeKidExcelRowDto;
import ru.abondin.hreasy.platform.service.admin.imp.ExcelImporter;

/**
 * Create or update employees from external excel file
 */
@Component
@RequiredArgsConstructor
@Getter
@Slf4j
public class AdminEmployeeKidsExcelImporter extends ExcelImporter<EmployeeKidImportConfig, ImportEmployeeKidExcelRowDto> {

    private final String tableBeanName = "kids";
    private final String tableItemBeanName = "kid";

    @Override
    protected BusinessError validateMandatoryFields(EmployeeKidImportConfig config, ImportEmployeeKidExcelRowDto row) {
        if (StringUtils.isBlank(row.getParentEmail())) {
            return new BusinessError("errors.import.emailNotSet",
                    Integer.toString(row.getRowNumber()), config.getColumns().getParentEmail());
        }
        if (StringUtils.isBlank(row.getDisplayName())) {
            return new BusinessError("errors.import.kids.displayNameNotSet",
                    Integer.toString(row.getRowNumber()), config.getColumns().getDisplayName());
        }
        return null;
    }

    @Override
    protected void applyRowConfigMappings(EmployeeKidImportConfig config, SimpleBlockReaderImpl reader) {
        // 15 fields handled
        addSimpleMapping(reader, config, "parentEmail", config.getColumns().getParentEmail(), true);
        addSimpleMapping(reader, config, "displayName", config.getColumns().getDisplayName(), true);
        addSimpleMapping(reader, config, "birthday", config.getColumns().getBirthday(), false);
    }
}
