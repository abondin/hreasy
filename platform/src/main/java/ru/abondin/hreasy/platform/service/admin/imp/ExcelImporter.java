package ru.abondin.hreasy.platform.service.admin.imp;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.util.CellReference;
import org.jxls.reader.*;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.ImportEmployeeExcelRowDto;
import ru.abondin.hreasy.platform.service.admin.imp.dto.ExcelImportConfig;
import ru.abondin.hreasy.platform.service.admin.imp.dto.ExcelImportRowDto;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Create or update dtos from external excel file
 */
@Slf4j
public abstract class ExcelImporter<C extends ExcelImportConfig, R extends ExcelImportRowDto> {

    protected abstract String getTableBeanName();

    protected abstract String getTableItemBeanName();


    public Flux<R> importFromFile(C config, InputStream file) {
        var beans = new HashMap<String, Object>();
        var items = new ArrayList<R>();
        beans.put(getTableBeanName(), items);
        XLSReadStatus status = null;
        try {
            status = configureReader(config)
                    .read(file, beans);
        } catch (IOException | InvalidFormatException e) {
            log.error("Unable to import from excel", e);
            Flux.error(new BusinessError("errors.import.unexpectedError"));
        }
        if (!status.isStatusOK()) {
            return Flux.error(new BusinessError("errors.import.statusNotOk"));
        }
        // validate that email is properly set
        // add row number to simplify
        for (var i = 0; i < items.size(); i++) {
            var e = items.get(i);
            e.setRowNumber(i + config.getTableStartRow());
            var error = validateMandatoryFields(config, e);
            if (error != null) {
                return Flux.error(error);
            }
        }
        return Flux.fromIterable(items);
    }

    protected abstract BusinessError validateMandatoryFields(C config, R row);


    private XLSReader configureReader(C config) {
        var reader = new XLSReaderImpl();
        reader.addSheetReader(config.getSheetNumber() - 1, configureSheetReader(config));
        return reader;
    }

    private XLSSheetReader configureSheetReader(C config) {
        var emptyBlock = new SimpleBlockReaderImpl();
        emptyBlock.setStartRow(0);
        emptyBlock.setEndRow(Math.max(config.getTableStartRow() - 2, 0));
        var sheetReader = new XLSSheetReaderImpl();
        sheetReader.addBlockReader(emptyBlock);
        sheetReader.addBlockReader(configureLoopReader(config));
        return sheetReader;
    }

    private XLSBlockReader configureLoopReader(C config) {
        var loopBlock = new XLSForEachBlockReaderImpl();
        loopBlock.setStartRow(config.getTableStartRow() - 1);
        loopBlock.setEndRow(config.getTableStartRow() - 1);
        loopBlock.setItems(getTableBeanName());
        loopBlock.setVar(getTableItemBeanName());
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

    private XLSBlockReader loopSectionBlockReader(C config) {
        var reader = new SimpleBlockReaderImpl(config.getTableStartRow() - 1, config.getTableStartRow() - 1);
        applyRowConfigMappings(config, reader);
        return reader;
    }

    protected abstract void applyRowConfigMappings(C config, SimpleBlockReaderImpl reader);

    protected void addSimpleMapping(SimpleBlockReader reader, C config, String property, String column, boolean keyProp) {
        if (column == null) {
            return;
        }
        int columnIndex = getColumnIndex(column);
        var fullPropertyName = property;
        if (!keyProp) {
            fullPropertyName += ".raw";
        }
        var mapping = new BeanCellMapping(config.getTableStartRow() - 1, (short) columnIndex, getTableItemBeanName(), fullPropertyName);
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
