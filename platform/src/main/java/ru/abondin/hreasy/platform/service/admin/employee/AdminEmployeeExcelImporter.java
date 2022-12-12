package ru.abondin.hreasy.platform.service.admin.employee;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jxls.reader.*;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.service.admin.employee.dto.EmployeeImportConfig;
import ru.abondin.hreasy.platform.service.admin.employee.dto.ImportEmployeeExcelDto;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

/**
 * Create or update employees from external excel file
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminEmployeeExcelImporter {

    private final String tableBeanName = "employees";
    private final String tableItemBeanName = "employee";


    public Flux<ImportEmployeeExcelDto> importEmployees(int importFlowId, InputStream file) {
        var beans = new HashMap<String, Object>();
        var employees = new ArrayList<ImportEmployeeExcelDto>();
        beans.put("employees", employees);
        XLSReadStatus status = null;
        try {
            status = configureReader(new EmployeeImportConfig())
                    .read(file, beans);
        } catch (IOException | InvalidFormatException e) {
            log.error("Unable to import employees. Flow: " + importFlowId, e);
            Flux.error(new BusinessError("errors.import.unexpectedError", Integer.toString(importFlowId)));
        }
        if (!status.isStatusOK()) {
            return Flux.error(new BusinessError("errors.import.statusNotOk", Integer.toString(importFlowId)));
        }
        return Flux.fromIterable(employees);
    }

    private XLSReader configureReader(EmployeeImportConfig config) {
        var reader = new XLSReaderImpl();
        reader.addSheetReader(config.getSheetNumber() - 1, configureSheetReader(config));
        return reader;
    }

    private XLSSheetReader configureSheetReader(EmployeeImportConfig config) {
        var emptyBlock = new SimpleBlockReaderImpl();
        emptyBlock.setStartRow(0);
        emptyBlock.setEndRow(Math.max(config.getTableStartRow() - 2, 0));
        var sheetReader = new XLSSheetReaderImpl();
        sheetReader.addBlockReader(emptyBlock);
        sheetReader.addBlockReader(configureLoopReader(config));
        return sheetReader;
    }

    private XLSBlockReader configureLoopReader(EmployeeImportConfig config) {
        var loopBlock = new XLSForEachBlockReaderImpl();
        loopBlock.setStartRow(config.getTableStartRow() - 1);
        loopBlock.setEndRow(config.getTableStartRow() - 1);
        loopBlock.setItems(tableBeanName);
        loopBlock.setVar(tableItemBeanName);
        loopBlock.setVarType(ImportEmployeeExcelDto.class);

        var breakCondition = new SimpleSectionCheck();
        var rowCheck = new OffsetRowCheckImpl();
        var cellCheck = new OffsetCellCheckImpl();
        cellCheck.setValue("");
        rowCheck.addCellCheck(cellCheck);
        rowCheck.setOffset(0);
        breakCondition.addRowCheck(rowCheck);
        loopBlock.setLoopBreakCondition(breakCondition);

        loopBlock.addBlockReader(loopSectionBlockReader(config));
        return loopBlock;
    }

    private XLSBlockReader loopSectionBlockReader(EmployeeImportConfig config) {
        var reader = new SimpleBlockReaderImpl(config.getTableStartRow() - 1, config.getTableStartRow() - 1);
        // 15 fields handled
        addSimpleMapping(reader, config, "email", config.getEmailCell(), false, null);
        addSimpleMapping(reader, config, "externalErpId", config.getExternalErpId(), false, null);
        addSimpleMapping(reader, config, "displayName", config.getDisplayNameCell(), true, null);
        addSimpleMapping(reader, config, "documentNumber", config.getDocumentNumberCell(), true, null);
        addSimpleMapping(reader, config, "documentSeries", config.getDocumentSeriesCell(), true, null);
        addSimpleMapping(reader, config, "documentIssuedBy", config.getDocumentIssuedByCell(), true, null);
        addSimpleMapping(reader, config, "documentIssuedDate", config.getDocumentIssuedDateCell(), true, null);
        addSimpleMapping(reader, config, "birthday", config.getBirthdayCell(), true, Date.class.getCanonicalName());
        addSimpleMapping(reader, config, "department", config.getDepartmentCell(), true, null);
        addSimpleMapping(reader, config, "phone", config.getPhoneCell(), true, null);
        addSimpleMapping(reader, config, "sex", config.getSexCell(), true, null);
        addSimpleMapping(reader, config, "registrationAddress", config.getRegistrationAddressCell(), true, null);
        addSimpleMapping(reader, config, "position", config.getPositionCell(), true, null);
        addSimpleMapping(reader, config, "dateOfEmployment", config.getDateOfEmploymentCell(), true, null);
        addSimpleMapping(reader, config, "dateOfDismissal", config.getDateOfDismissalCell(), true, null);
        return reader;
    }

    private void addSimpleMapping(SimpleBlockReader reader, EmployeeImportConfig config, String property, Short columnNumber, boolean nullAllowed, String type) {
        if (columnNumber == null || columnNumber <= 0) {
            return;
        }
        var mapping = new BeanCellMapping(config.getTableStartRow() - 1, (short) (columnNumber - 1), tableItemBeanName, property + ".raw");
        mapping.setNullAllowed(nullAllowed);
        mapping.setType(Optional.ofNullable(type).orElse(String.class.getCanonicalName()));
        reader.addMapping(mapping);
    }


}
