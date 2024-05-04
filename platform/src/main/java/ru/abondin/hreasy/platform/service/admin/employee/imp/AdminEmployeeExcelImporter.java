package ru.abondin.hreasy.platform.service.admin.employee.imp;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jxls.reader.SimpleBlockReaderImpl;
import org.springframework.stereotype.Component;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.EmployeeImportConfig;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.ImportEmployeeExcelRowDto;
import ru.abondin.hreasy.platform.service.admin.imp.ExcelImporter;

/**
 * Create or update employees from external excel file
 */
@Component
@RequiredArgsConstructor
@Getter
@Slf4j
public class AdminEmployeeExcelImporter extends ExcelImporter<EmployeeImportConfig, ImportEmployeeExcelRowDto> {

    private final String tableBeanName = "employees";
    private final String tableItemBeanName = "employee";


    @Override
    protected Class<ImportEmployeeExcelRowDto> getRowClass() {
        return ImportEmployeeExcelRowDto.class;
    }

    @Override
    protected BusinessError validateMandatoryFields(EmployeeImportConfig config, ImportEmployeeExcelRowDto row) {
        if (StringUtils.isBlank(row.getEmail())) {
            return new BusinessError("errors.import.emailNotSet",
                    Integer.toString(row.getRowNumber()), config.getColumns().getEmail());
        }
        return null;
    }

    @Override
    protected void applyRowConfigMappings(EmployeeImportConfig config, SimpleBlockReaderImpl reader) {
        // 15 fields handled
        addSimpleMapping(reader, config, "email", config.getColumns().getEmail(), true);
        addSimpleMapping(reader, config, "externalErpId", config.getColumns().getExternalErpId(), false);
        addSimpleMapping(reader, config, "displayName", config.getColumns().getDisplayName(), false);
        addSimpleMapping(reader, config, "documentNumber", config.getColumns().getDocumentNumber(), false);
        addSimpleMapping(reader, config, "documentSeries", config.getColumns().getDocumentSeries(), false);
        addSimpleMapping(reader, config, "documentIssuedBy", config.getColumns().getDocumentIssuedBy(), false);
        addSimpleMapping(reader, config, "documentIssuedDate", config.getColumns().getDocumentIssuedDate(), false);
        addSimpleMapping(reader, config, "birthday", config.getColumns().getBirthday(), false);
        addSimpleMapping(reader, config, "department", config.getColumns().getDepartment(), false);
        addSimpleMapping(reader, config, "phone", config.getColumns().getPhone(), false);
        addSimpleMapping(reader, config, "sex", config.getColumns().getSex(), false);
        addSimpleMapping(reader, config, "registrationAddress", config.getColumns().getRegistrationAddress(), false);
        addSimpleMapping(reader, config, "organization", config.getColumns().getOrganization(), false);
        addSimpleMapping(reader, config, "position", config.getColumns().getPosition(), false);
        addSimpleMapping(reader, config, "dateOfEmployment", config.getColumns().getDateOfEmployment(), false);
        addSimpleMapping(reader, config, "dateOfDismissal", config.getColumns().getDateOfDismissal(), false);
    }
}
