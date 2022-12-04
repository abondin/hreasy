package ru.abondin.hreasy.platform.service.admin.employee;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jxls.reader.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.service.admin.employee.dto.EmployeeImportConfig;
import ru.abondin.hreasy.platform.service.admin.employee.dto.ImportEmployeeExcelDto;

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

    @Value("${classpath:jxls/import_employees_template.xml}")
    private final Resource template;

    private final String tableBeanName = "employees";
    private final String tableItemBeanName = "employee";

    public Flux<ImportEmployeeExcelDto> importEmployees(InputStream excel) throws IOException {
        try {
            var employees = new ArrayList<ImportEmployeeExcelDto>();
            var beans = new HashMap<String, Object>();
            beans.put(tableBeanName, employees);
            var readStatus = ReaderBuilder.buildFromXML(template.getInputStream()).read(excel, beans);
            log.info("Import employee: Read excel file using template status {}: {}", readStatus.isStatusOK(), readStatus.getReadMessages());
            return Flux.fromIterable(employees);
        } catch (InvalidFormatException | SAXException e) {
            throw new IOException(e);
        }
    }


    public XLSReader configureReader(EmployeeImportConfig config) {
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
        reader.addMapping(simpleMapping("email", config.getEmailCell()));
        reader.addMapping(simpleMapping("displayName", config.getDisplayNameCell()));
        return reader;
    }

    private BeanCellMapping simpleMapping(String property, short columnNumber) {
        return new BeanCellMapping(0, (short) (columnNumber - 1), tableItemBeanName, property);
    }


}
