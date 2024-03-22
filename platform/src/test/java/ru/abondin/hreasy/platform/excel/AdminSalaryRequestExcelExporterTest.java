package ru.abondin.hreasy.platform.excel;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.service.salary.AdminSalaryRequestExcelExporter;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestExportDto;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestType;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.ArrayList;

@Slf4j
public class AdminSalaryRequestExcelExporterTest {

    String[] states = {"In progress", "Implemented", "Rejected"};
    private AdminSalaryRequestExcelExporter.AdminSalaryRequestExportBundle bundle;

    private AdminSalaryRequestExcelExporter exporter;

    @BeforeEach
    public void before() {
        var requests = new ArrayList<SalaryRequestExportDto>();
        int size = 300;
        for (int i = 1; i <= size; i++) {
            var request = new SalaryRequestExportDto();
            request.setEmployee("Employee " + i);
            request.setTypeValue(i <= size / 10 ? SalaryRequestType.BONUS.getValue() : SalaryRequestType.SALARY_INCREASE.getValue());
            request.setBudgetBusinessAccount("Business Account " + i);
            request.setBudgetExpectedFundingUntil(LocalDate.now().plusDays(i));
            request.setAssessment("Assessment " + i);
            request.setEmployeePosition("Position " + i);
            request.setEmployeeProject("Project " + i);
            request.setEmployeeProjectRole("Project Role " + i);
            request.setEmployeeBusinessAccount("Employee Business Account " + i);
            request.setCreatedAt(OffsetDateTime.now());
            request.setCreatedBy("User " + i);
            request.setCurrentSalaryAmount(BigDecimal.valueOf(i * 1000));
            request.setReqIncreaseAmount(BigDecimal.valueOf(i * 500));
            request.setReqPlannedSalaryAmount(BigDecimal.valueOf(i * 1500));
            request.setReqReason("Reason " + i);
            request.setImplIncreaseAmount(BigDecimal.valueOf(i * 300));
            request.setImplSalaryAmount(BigDecimal.valueOf(i * 1200));
            request.setImplIncreaseStartPeriod(YearMonth.of(2024, 3));
            request.setImplRejectReason("Reject Reason " + i);
            request.setImplState(states[i % 3]);
            request.setImplNewPosition("New Position " + i);
            request.setImplemented("Implemented " + i);

            requests.add(request);
        }

        this.bundle = AdminSalaryRequestExcelExporter.AdminSalaryRequestExportBundle.builder()
                .exportTime(OffsetDateTime.now())
                .exportedBy("JUnit test")
                .period(202402)
                .requests(requests)
                .build();

        exporter = new AdminSalaryRequestExcelExporter(new ClassPathResource("jxls/admin_salary_requests_template.xlsx"),
                new I18Helper.DummyI18Helper());
    }

    @Test
    public void testSalaryRequestsExcelGeneration() throws Exception {
        log.info("Export test salary requests to target/salary_requests_out.xlsx");
        try (var fileOut = new FileOutputStream("target/salary_requests_out.xlsx")) {
            exporter.exportSalaryRequests(bundle, fileOut);
        }
    }

    // Additional test methods can be added here

}
