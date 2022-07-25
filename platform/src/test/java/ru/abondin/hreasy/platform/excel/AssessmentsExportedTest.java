package ru.abondin.hreasy.platform.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import ru.abondin.hreasy.platform.service.assessment.AssessmentsSummaryExcelExporter;
import ru.abondin.hreasy.platform.service.assessment.dto.EmployeeAssessmentsSummary;
import ru.abondin.hreasy.platform.service.dto.CurrentProjectDictDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class AssessmentsExportedTest {

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
            new CurrentProjectDictDto(3, "Одноклассники", null)
    );

    private final List<SimpleDictDto> bas = Arrays.asList(
            new SimpleDictDto(1, "Clouds"),
            new SimpleDictDto(2, "RnD")
    );

    private AssessmentsSummaryExcelExporter.AssessmentsSummaryExportBundle bundle;


    private AssessmentsSummaryExcelExporter exporter;

    @BeforeEach
    public void before() {
        var assessments = new ArrayList<EmployeeAssessmentsSummary>();
        int size = 100;
        for (int i = 1; i <= size; i++) {
            var lastname = lastNames.get((int) (Math.random() * lastNames.size()));
            var firstame = firstNames.get((int) (Math.random() * firstNames.size()));
            var patronicname = patronicNames.get((int) (Math.random() * patronicNames.size()));
            var displayName = Strings.join(Arrays.asList(lastname, firstame, patronicname), ' ');

            var summary = new EmployeeAssessmentsSummary();
            summary.setCurrentProject(i % 5 == 0 ? null : projects.get((int) (Math.random() * projects.size())));
            summary.setBa(i % 4 == 0 ? null : bas.get((int) (Math.random() * bas.size())));
            summary.setDaysWithoutAssessment((long) (Math.random() * 500));
            summary.setDisplayName(displayName);
            summary.setEmployeeId(i);
            summary.setLastAssessmentDate(i % 3 == 0 ? null : LocalDate.now().minusDays((long) (2 + Math.random() * 100)));
            summary.setLastAssessmentCompletedDate((i % 3 == 0 || i % 4 == 0) ? null :
                    summary.getLastAssessmentDate().plusDays(2));
            summary.setLastAssessmentId(i % 3 == 0 ? null : i + size);
            summary.setEmployeeDateOfEmployment(LocalDate.now().minusMonths((long) (5 + Math.random() * 30)));

            assessments.add(summary);
        }
        this.bundle = AssessmentsSummaryExcelExporter.AssessmentsSummaryExportBundle.builder()
                .exportTime(OffsetDateTime.now())
                .assessments(assessments)
                .build();

        exporter = new AssessmentsSummaryExcelExporter();
        exporter.setTemplate(new ClassPathResource("jxls/assessments_summary_template.xlsx"));
    }

    @Test
    @Disabled("Nothing to tests yet. It is only to play with apache poi")
    public void testAssessmentsSummary() throws Exception {
        log.info("Export test assessments summary to target/assessments_summary_out.xlsx");
        try (var fileOut = new FileOutputStream("target/assessments_summary_out.xlsx")) {
            exporter.exportAssessmentsSummary(bundle, fileOut);
        }
    }


}
