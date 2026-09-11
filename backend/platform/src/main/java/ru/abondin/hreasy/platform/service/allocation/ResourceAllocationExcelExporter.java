package ru.abondin.hreasy.platform.service.allocation;

import lombok.Data;
import lombok.Setter;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationAnalyticsDto;
import ru.abondin.hreasy.platform.service.dto.ProjectWorkstreamDto;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Flat allocation workbook: one numeric row per employee/project/workstream, without subtotals.
 */
@Component
public class ResourceAllocationExcelExporter {
    @Setter
    @Value("classpath:jxls/resource_allocations_template.xlsx")
    private Resource template;

    private record RowKey(Integer employeeId, Integer projectId, Integer workstreamId) {
    }

    @Data
    public static class ExportRow {
        private String businessAccount;
        private String project;
        private String workstream;
        private String employee;
        private String email;
        private String currentProjectRole;
        private BigDecimal yearTotal = BigDecimal.ZERO;
        private final BigDecimal[] months = new BigDecimal[12];
    }

    /**
     * Both units store fractions numerically; Excel's percentage format determines presentation.
     */
    public void export(ResourceAllocationAnalyticsDto analytics, boolean percentages,
                       OffsetDateTime exportedAt, String exportedBy, OutputStream output) throws IOException {
        var context = new Context();
        context.putVar("year", analytics.year());
        context.putVar("unit", percentages ? "Проценты" : "Человеко-месяцы");
        context.putVar("exportedAt", exportedAt.toLocalDateTime());
        context.putVar("exportedBy", exportedBy);
        context.putVar("rows", rows(analytics));
        try (var input = template.getInputStream()) {
            JxlsHelper.getInstance().processTemplate(input, output, context);
        }
    }

    private List<ExportRow> rows(ResourceAllocationAnalyticsDto analytics) {
        var employees = analytics.employees().stream().collect(Collectors.toMap(
                ResourceAllocationAnalyticsDto.EmployeeDto::id, Function.identity()));
        var projects = analytics.projects().stream().collect(Collectors.toMap(
                ResourceAllocationAnalyticsDto.ProjectDto::id, Function.identity()));
        var workstreams = analytics.workstreams().stream().collect(Collectors.toMap(
                ProjectWorkstreamDto::id, Function.identity()));
        var rows = new HashMap<RowKey, ExportRow>();
        for (var allocation : analytics.allocations()) {
            var key = new RowKey(allocation.employeeId(), allocation.projectId(), allocation.workstreamId());
            var row = rows.computeIfAbsent(key, ignored -> {
                var employee = Objects.requireNonNull(employees.get(key.employeeId()), "Allocation employee missing");
                var project = Objects.requireNonNull(projects.get(key.projectId()), "Allocation project missing");
                var result = new ExportRow();
                result.setBusinessAccount(project.baName());
                result.setProject(project.name());
                result.setWorkstream(key.workstreamId() == null ? null : Objects.requireNonNull(
                        workstreams.get(key.workstreamId()), "Allocation workstream missing").displayName());
                result.setEmployee(employee.displayName());
                result.setEmail(employee.email());
                result.setCurrentProjectRole(employee.currentProjectRole());
                return result;
            });
            int month = allocation.period() - analytics.year() * 100;
            var value = BigDecimal.valueOf(allocation.percent(), 2);
            row.months[month] = row.months[month] == null ? value : row.months[month].add(value);
            row.yearTotal = row.yearTotal.add(value);
        }
        Comparator<String> textOrder = Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER);
        return rows.values().stream().sorted(Comparator.comparing(ExportRow::getBusinessAccount, textOrder)
                .thenComparing(ExportRow::getProject, textOrder)
                .thenComparing(ExportRow::getWorkstream, textOrder)
                .thenComparing(ExportRow::getEmployee, textOrder)
                .thenComparing(ExportRow::getEmail, textOrder)).toList();
    }
}
