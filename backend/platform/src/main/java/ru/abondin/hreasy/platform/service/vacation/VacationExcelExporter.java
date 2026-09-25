package ru.abondin.hreasy.platform.service.vacation;

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
import ru.abondin.hreasy.platform.service.vacation.dto.VacationExportDto;

import java.io.IOException;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Export vacations as excel formatted table
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class VacationExcelExporter {

    private final I18Helper i18Helper;

    @Setter
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
            var context = new HashMap<String, Object>();
            context.put("vacations", bundle.getVacations());
            context.put("years", bundle.getYears());
            context.put("exportedAt", i18Helper.formatDateTime(bundle.getLocale(), bundle.getExportTime()));
            JxlsPoiTemplateFillerBuilder.newInstance().withTemplate(is).buildAndFill(context, () -> out);
        }
    }
}
