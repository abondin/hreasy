package ru.abondin.hreasy.platform.service.admin.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jxls.common.Context;
import org.jxls.reader.ReaderBuilder;
import org.jxls.reader.XLSReader;
import org.jxls.util.JxlsHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.service.admin.employee.dto.CreateOrUpdateEmployeeBody;
import ru.abondin.hreasy.platform.service.admin.employee.dto.EmployeeExportDto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Create or update employees from external excel file
 */
@Component
@Slf4j
public class AdminEmployeeExcelImporter {

    @Value("${classpath:jxls/import_employees_template.xml}")
    private Resource template;

    public Flux<CreateOrUpdateEmployeeBody> importEmployees(InputStream excel) throws IOException {
        ReaderBuilder.buildFromXML()
        return null ;
    }
}
