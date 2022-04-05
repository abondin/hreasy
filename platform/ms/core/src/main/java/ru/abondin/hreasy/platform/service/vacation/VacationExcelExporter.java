package ru.abondin.hreasy.platform.service.vacation;

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
import ru.abondin.hreasy.platform.service.vacation.dto.VacationExportDto;

import java.io.IOException;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Export vacations as excel formatted table
 */
@Component
@Slf4j
@RequiredArgsConstructor
@AllArgsConstructor
public class VacationExcelExporter {

    @Value("${classpath:jxls/vacations_template.xlsx}")
    private Resource template;

    @Data
    @Builder
    public static class VacationsExportBundle {
        private final List<Integer> years;
        private String exportedBy;
        private List<VacationExportDto> vacations;
        private OffsetDateTime exportTime;
        private Locale locale;
    }


    public void exportVacations(VacationsExportBundle bundle, OutputStream out) throws IOException {
        try (var is = template.getInputStream()) {
            var context = new Context();
            context.putVar("vacations", bundle.getVacations());
            context.putVar("years", bundle.getYears());
            context.putVar("exportedAt", bundle.getExportTime().toLocalDateTime());
            JxlsHelper.getInstance().processTemplate(is, out, context);
        }
    }
}
