package ru.abondin.hreasy.platform.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.service.dto.CurrentProjectDictDto;
import ru.abondin.hreasy.platform.service.dto.EmployeeDto;
import ru.abondin.hreasy.platform.service.overtime.OvertimeReportExcelExporter;
import ru.abondin.hreasy.platform.service.overtime.dto.OvertimeEmployeeSummary;

import java.io.FileOutputStream;
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

    private final List<CurrentProjectDictDto> projects = Arrays.asList(
            new CurrentProjectDictDto(1, "Facebook", "Developer"),
            new CurrentProjectDictDto(2, "VK", "QA Lead"),
            new CurrentProjectDictDto(3, "Одноклассники", "PM")
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
            var employee = new EmployeeDto(i, displayName, "мужской", null,
                    projects.get((int) (Math.random() * projects.size()))
                    , null, null, null
                    , lastname + "." + firstame + "@company.org"
                    , null,  false, new ArrayList<>());
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

        exporter = new OvertimeReportExcelExporter(new I18Helper.DummyI18Helper());
        exporter.setTemplate(new ClassPathResource("jxls/overtimes_summary_template.xlsx"));
    }

    private static float roundToHalf(double d) {
        return (float) (Math.round(d * 2) / 2.0);
    }

    @Test
    @Disabled("Nothing to tests yet. It is only to play with apache poi")
    public void testSummary() throws Exception {
        log.info("Export test overtimes to target/overtimes_out.xlsx");
        try (var fileOut = new FileOutputStream("target/overtimes_out.xlsx")) {
            exporter.exportReportForPeriod(bundle, fileOut);
        }
    }


}
