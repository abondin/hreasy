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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

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
        if (status.isStatusOK()) {
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
        // 14 fields handled
        addSimpleMapping(reader, "email", config.getEmailCell(), false, null);
        addSimpleMapping(reader, "displayName", config.getDisplayNameCell(), true, null);
        addSimpleMapping(reader, "documentNumber", config.getDocumentNumberCell(), true, null);
        addSimpleMapping(reader, "documentSeries", config.getDocumentSeriesCell(), true, null);
        addSimpleMapping(reader, "documentIssuedBy", config.getDocumentIssuedByCell(), true, null);
        addSimpleMapping(reader, "documentIssuedDate", config.getDocumentIssuedDateCell(), true, LocalDate.class.getCanonicalName());
        addSimpleMapping(reader, "birthday", config.getBirthdayCell(), true, LocalDate.class.getCanonicalName());
        addSimpleMapping(reader, "departmentName", config.getDepartmentNameCell(), true, null);
        addSimpleMapping(reader, "phone", config.getPhoneCell(), true, null);
        addSimpleMapping(reader, "sex", config.getSexCell(), true, null);
        addSimpleMapping(reader, "registrationAddress", config.getRegistrationAddressCell(), true, null);
        addSimpleMapping(reader, "positionName", config.getPositionNameCell(), true, null);
        addSimpleMapping(reader, "displayName", config.getDisplayNameCell(), true, null);
        addSimpleMapping(reader, "displayName", config.getDisplayNameCell(), true, null);
        return reader;
    }

    private void addSimpleMapping(SimpleBlockReader reader, String property, Short columnNumber, boolean nullAllowed, String type) {
        if (columnNumber == null || columnNumber <= 0) {
            return;
        }
        var mapping = new BeanCellMapping(0, (short) (columnNumber - 1), tableItemBeanName, property + ".raw");
        mapping.setNullAllowed(nullAllowed);
        if (type != null) {
            mapping.setType(type);
        }
    }


}
