package ru.abondin.hreasy.platform.service.salary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jxls.transform.poi.JxlsPoiTemplateFillerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestExportDto;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestType;

import java.io.IOException;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Export all information about salaries requests for given period
 */
@Component
@Slf4j
@RequiredArgsConstructor
@AllArgsConstructor
public class AdminSalaryRequestExcelExporter {


    @Value("${classpath:jxls/admin_salary_requests_template.xlsx}")
    private Resource template;

    @Autowired
    private I18Helper i18Helper;

    @Data
    @Builder
    public static class AdminSalaryRequestExportBundle {
        private String exportedBy;
        private Integer period;
        private OffsetDateTime exportTime;
        @Builder.Default
        private List<SalaryRequestExportDto> requests = new ArrayList<>();
        private Locale locale;
    }


    public void exportSalaryRequests(AdminSalaryRequestExportBundle bundle, OutputStream out) throws IOException {
        try (var is = template.getInputStream()) {
            var context = new HashMap<String, Object>();
            context.put("requests", i18n(bundle.locale, bundle.requests, r -> SalaryRequestType.SALARY_INCREASE.getValue() == r.getTypeValue()));
            context.put("bonuses", i18n(bundle.locale, bundle.requests, r -> SalaryRequestType.BONUS.getValue() == r.getTypeValue()));
            context.put("exportedAt", i18Helper.formatDateTime(bundle.getLocale(), bundle.getExportTime()));
            context.put("exportedBy", bundle.getExportedBy());
            context.put("period", MapperBase.fromPeriodId(bundle.getPeriod()));
            JxlsPoiTemplateFillerBuilder.newInstance().withTemplate(is).buildAndFill(context, () -> out);
        }
    }

    private List<SalaryRequestExportDto> i18n(Locale locale, Collection<SalaryRequestExportDto> requests, Predicate<SalaryRequestExportDto> filter) {
        return requests.stream().filter(filter).map(r -> {
            r.setType(i18Helper.localize(locale, "enum.SalaryRequestType." + r.getType()));
            if (r.getImplState() == null) {
                r.setImplState(i18Helper.localize(locale, "enum.SalaryRequestImplementationState.NIL"));
            } else {
                r.setImplState(i18Helper.localize(locale, "enum.SalaryRequestImplementationState." + r.getImplState()));
            }
            return r;
        }).collect(Collectors.toList());
    }
}
