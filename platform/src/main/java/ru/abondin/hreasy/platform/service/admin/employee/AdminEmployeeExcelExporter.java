package ru.abondin.hreasy.platform.service.admin.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import ru.abondin.hreasy.platform.service.admin.employee.dto.EmployeeExportDto;

import java.io.IOException;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Export all information about employees as an excel formatted table
 */
@Component
@Slf4j
@RequiredArgsConstructor
@AllArgsConstructor
public class AdminEmployeeExcelExporter {

    @Value("${classpath:jxls/admin_employees_template.xlsx}")
    private Resource template;

    @Data
    @Builder
    public static class AdminEmployeeExportBundle {
        private String exportedBy;
        private OffsetDateTime exportTime;
        private List<EmployeeExportDto> employees;
        private Locale locale;
    }


    public void exportEmployees(AdminEmployeeExportBundle bundle, OutputStream out) throws IOException {
        try (var is = template.getInputStream()) {
            var context = new Context();
            context.putVar("employees", bundle.getEmployees());
            context.putVar("exportedAt", bundle.getExportTime().toLocalDateTime());
            JxlsHelper.getInstance().processTemplate(is, out, context);
        }
    }
}
