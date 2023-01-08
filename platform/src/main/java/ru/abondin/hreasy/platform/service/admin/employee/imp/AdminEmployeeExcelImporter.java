package ru.abondin.hreasy.platform.service.admin.employee.imp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.util.CellReference;
import org.jxls.reader.*;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.EmployeeImportConfig;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.ImportEmployeeExcelRowDto;

import java.io.IOException;
import java.io.InputStream;
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


    public Flux<ImportEmployeeExcelRowDto> importEmployees(EmployeeImportConfig config, InputStream file) {
        var beans = new HashMap<String, Object>();
        var employees = new ArrayList<ImportEmployeeExcelRowDto>();
        beans.put("employees", employees);
        XLSReadStatus status = null;
        try {
            status = configureReader(new EmployeeImportConfig())
                    .read(file, beans);
        } catch (IOException | InvalidFormatException e) {
            log.error("Unable to import employees", e);
            Flux.error(new BusinessError("errors.import.unexpectedError"));
        }
        if (!status.isStatusOK()) {
            return Flux.error(new BusinessError("errors.import.statusNotOk"));
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
        loopBlock.setVarType(ImportEmployeeExcelRowDto.class);

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
        addSimpleMapping(reader, config, "position", config.getColumns().getPosition(), false);
        addSimpleMapping(reader, config, "dateOfEmployment", config.getColumns().getDateOfEmployment(), false);
        addSimpleMapping(reader, config, "dateOfDismissal", config.getColumns().getDateOfDismissal(), false);
        return reader;
    }

    private void addSimpleMapping(SimpleBlockReader reader, EmployeeImportConfig config, String property, String column, boolean keyProp) {
        if (column == null) {
            return;
        }
        int columnIndex = getColumnIndex(column);
        var fullPropertyName = property;
        if (!keyProp){
            fullPropertyName += ".raw";
        }
        var mapping = new BeanCellMapping(config.getTableStartRow() - 1, (short) columnIndex, tableItemBeanName, fullPropertyName);
        mapping.setNullAllowed(!keyProp);
        mapping.setType(String.class.getCanonicalName());
        reader.addMapping(mapping);
    }

    /**
     * Accept column name in letter format (AN) or column number format (40)
     *
     * @param column
     * @return
     */
    public static int getColumnIndex(String column) {
        try {
            return Integer.parseInt(column) - 1;
        } catch (NumberFormatException ex) {
            return CellReference.convertColStringToIndex(column);
        }
    }


}
