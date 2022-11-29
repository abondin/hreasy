package ru.abondin.hreasy.platform.service.admin.employee;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jxls.reader.ReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;
import reactor.core.publisher.Flux;
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

    public Flux<ImportEmployeeExcelDto> importEmployees(InputStream excel) throws IOException {
        try {
            var employees = new ArrayList<ImportEmployeeExcelDto>();
            var beans = new HashMap<String, Object>();
            beans.put("employees", employees);
            var readStatus = ReaderBuilder.buildFromXML(template.getInputStream()).read(excel, beans);
            log.info("Import employee: Read excel file using template status {}: {}", readStatus.isStatusOK(), readStatus.getReadMessages());
            return Flux.fromIterable(employees);
        } catch (InvalidFormatException | SAXException e) {
            throw new IOException(e);
        }
    }
}
