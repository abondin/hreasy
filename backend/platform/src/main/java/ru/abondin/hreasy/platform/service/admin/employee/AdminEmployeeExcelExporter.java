package ru.abondin.hreasy.platform.service.admin.employee;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jxls.transform.poi.JxlsPoiTemplateFillerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.service.admin.employee.dto.EmployeeExportDto;

import java.io.IOException;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Export all information about employees as an excel formatted table
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AdminEmployeeExcelExporter {

    private final I18Helper i18Helper;

    @Setter
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
            var context = new HashMap<String, Object>();
            context.put("employees", bundle.getEmployees());
            context.put("exportedAt", i18Helper.formatDateTime(bundle.getLocale(), bundle.getExportTime()));
            JxlsPoiTemplateFillerBuilder.newInstance().withTemplate(is).buildAndFill(context, () -> out);
        }
    }
}
