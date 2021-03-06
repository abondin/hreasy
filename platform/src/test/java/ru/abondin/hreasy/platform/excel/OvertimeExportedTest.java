package ru.abondin.hreasy.platform.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.service.dto.EmployeeDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.overtime.OvertimeReportExcelExporter;
import ru.abondin.hreasy.platform.service.overtime.dto.OvertimeEmployeeSummary;

import java.io.File;
import java.io.FileOutputStream;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class OvertimeExportedTest {

    private final List<String> firstNames = Arrays.asList(
            "Иван", "Василий", "Александр", "Сергей", "Валерий", "Никита"
    );
    private final List<String> lastNames = Arrays.asList(
            "Чайкин", "Снигерёв", "Уткин", "Гусев", "Синицин", "Лебедев"
    );
    private final List<String> patronicNames = Arrays.asList(
            null, "Васильевич", "Витальевич", "Владимирович", "Сергеевич", "Александрович"
    );

    private final List<SimpleDictDto> projects = Arrays.asList(
            new SimpleDictDto(1, "Facebook"),
            new SimpleDictDto(2, "VK"),
            new SimpleDictDto(3, "Одноклассники")
    );


    private OvertimeReportExcelExporter.OvertimeExportBundle bundle;


    private OvertimeReportExcelExporter exporter;

    @BeforeEach
    public void before() {
        var employees = new ArrayList<EmployeeDto>();
        var overtimes = new ArrayList<OvertimeEmployeeSummary>();
        int size = 100;
        for (int i = 1; i <= size; i++) {
            var lastname = lastNames.get((int) (Math.random() * lastNames.size()));
            var firstame = firstNames.get((int) (Math.random() * firstNames.size()));
            var patronicname = patronicNames.get((int) (Math.random() * patronicNames.size()));
            var displayName = Strings.join(Arrays.asList(lastname, firstame, patronicname), ' ');
            var employee = new EmployeeDto(i, lastname, firstame, patronicname, displayName, null, "мужской", null,
                    projects.get((int) (Math.random() * projects.size())), null, null, null, null, null, false, new ArrayList<>());
            employees.add(employee);

            var report = new OvertimeEmployeeSummary();
            report.setReportId(size + i);
            report.setEmployeeId(i);
            report.setTotalHours(roundToHalf((Math.random() * 24)));
            overtimes.add(report);
        }
        this.bundle = OvertimeReportExcelExporter.OvertimeExportBundle.builder()
                .exportTime(OffsetDateTime.now())
                .overtimes(overtimes)
                .employees(employees)
                .period(202100)
                .projects(projects)
                .build();

        exporter = new OvertimeReportExcelExporter(new I18Helper() {
            @Override
            public String localize(String code, Object... args) {
                return code;
            }
        });
    }

    private static float roundToHalf(double d) {
        return (float) (Math.round(d * 2) / 2.0);
    }

    @Test
    @Disabled("Nothing to tests yet. It is only to play with apache poi")
    public void testSummary() throws Exception {
        var destination = File.createTempFile("simpletable", ".xlsx");
        log.debug("Writing test file to {}", destination);
        // Save
        try (var fileOut = new FileOutputStream(destination)) {
            exporter.exportReportForPeriod(bundle, fileOut);
        }

    }


}
