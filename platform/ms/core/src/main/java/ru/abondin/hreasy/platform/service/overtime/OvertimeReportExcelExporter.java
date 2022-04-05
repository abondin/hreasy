package ru.abondin.hreasy.platform.service.overtime;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.service.dto.EmployeeDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.overtime.dto.OvertimeEmployeeSummary;

import java.io.IOException;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Export overtime report and save it to disk
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OvertimeReportExcelExporter {

    private final I18Helper i18Helper;

    @Setter
    @Value("${classpath:jxls/overtimes_summary_template.xlsx}")
    private Resource template;

    @Data
    @Builder
    public static class OvertimeExportBundle {
        private final int period;
        private List<OvertimeEmployeeSummary> overtimes;
        private List<EmployeeDto> employees;
        private List<SimpleDictDto> projects;
        private OffsetDateTime exportTime;
        private Locale locale;
    }

    @Data
    public static class OvertimeSummaryExportDto {
        private String employeeDisplayName;
        private String employeeCurrentProject;
        private float totalHours;
        private String commonApprovalStatus;
    }


    public void exportReportForPeriod(OvertimeExportBundle bundle, OutputStream out) throws IOException {
        var overtimeSummaryExports = getOvertimeSummaryExports(bundle);
        try (var is = template.getInputStream()) {
            var context = new Context();
            context.putVar("overtimes", overtimeSummaryExports);
            context.putVar("period", YearMonth.of(bundle.getPeriod() / 100, bundle.getPeriod() % 100 + 1));
            context.putVar("exportedAt", bundle.getExportTime().toLocalDateTime());
            JxlsHelper.getInstance().processTemplate(is, out, context);
        }
    }


    private List<OvertimeSummaryExportDto> getOvertimeSummaryExports(OvertimeExportBundle bundle) {
        var result = new ArrayList<OvertimeSummaryExportDto>();
        for (var e : bundle.employees.stream().sorted(Comparator.comparing(EmployeeDto::getDisplayName)).collect(Collectors.toList())) {
            var report = bundle.overtimes.stream()
                    .filter(r -> r.getEmployeeId() == e.getId()).findFirst().orElse(null);
            if (report == null) {
                // no overtimes for this employee
                continue;
            }
            var exportLine = new OvertimeSummaryExportDto();
            exportLine.setEmployeeDisplayName(e.getDisplayName());
            if (e.getCurrentProject() != null) {
                exportLine.setEmployeeCurrentProject(e.getCurrentProject().getName());
            }
            if (report.getCommonApprovalStatus() != null) {
                exportLine.setCommonApprovalStatus(i18Helper.localize(bundle.locale, "enum.OvertimeApprovalCommonStatus." + report.getCommonApprovalStatus().toString()));
            }
            exportLine.setTotalHours(report.getTotalHours());
            result.add(exportLine);
        }
        return result;
    }


    public static String formatPeriod(int periodId) {
        var ym = YearMonth.of(periodId / 100, periodId % 100 + 1);
        return ym.format(DateTimeFormatter.ofPattern("MM/yyyy", new Locale("ms/core/src/test/java/ru")));
    }
}
