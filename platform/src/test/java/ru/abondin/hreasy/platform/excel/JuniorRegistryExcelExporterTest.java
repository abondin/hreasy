package ru.abondin.hreasy.platform.excel;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.service.udr.AdminJuniorRegistryExcelExporter;
import ru.abondin.hreasy.platform.service.udr.dto.JuniorExportDto;

import java.io.FileOutputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Random;

@Slf4j
public class JuniorRegistryExcelExporterTest {

    private AdminJuniorRegistryExcelExporter.AdminJuniorRegistryExportBundle bundle;

    private AdminJuniorRegistryExcelExporter exporter;

    @BeforeEach
    public void before() {
        var items = new ArrayList<JuniorExportDto>();
        var random = new Random();
        int size = 300;
        for (int i = 1; i <= size; i++) {
            var item = new JuniorExportDto();
            item.setId(i);
            item.setJuniorEmpl("Junior " + i);
            item.setMentor(i % 5 == 0 ? null : "Mentor " + random.nextInt(1, 10));
            item.setBudgetingAccount(i % 7 == 0 ? null : "BA " + random.nextInt(1, 3));
            item.setCurrentProject(i % 10 == 0 ? null : "Project " + random.nextInt(1, 10));
            item.setRole("Role " + random.nextInt(1, 5));
            item.setJuniorInCompanyMonths((long) random.nextInt(1, 10));
            item.setMonthsWithoutReport((long) random.nextInt(1, 5));
            for (int j = 1; j <= i % 5; j++) {
                item.getReportsProgress().add((short) random.nextInt(1, 4));
                item.getReportsComment().add("Report Comment " + i);
                item.getReportsCreatedBy().add("Report Author" + i);
                item.getReportsCreatedAt().add(OffsetDateTime.now().toLocalDate());
            }
            if (i % 5 == 0) {
                item.setGraduatedAt(OffsetDateTime.now().toLocalDate());
                item.setGraduatedBy("Graduated by");
                item.setGraduatedComment("Graduated with excellent marks");
            }
            items.add(item);
        }

        this.bundle = AdminJuniorRegistryExcelExporter.AdminJuniorRegistryExportBundle.builder()
                .exportedAt(OffsetDateTime.now())
                .exportedBy("JUnit test")
                .items(items)
                .build();

        exporter = new AdminJuniorRegistryExcelExporter(new ClassPathResource("jxls/admin_junior_registry_template.xlsx"),
                new I18Helper.DummyI18Helper());
    }

    @Test
    public void testJuniorRegistryExcelGeneration() throws Exception {
        log.info("Export generated test data to target/junior_registry_out.xlsx");
        try (var fileOut = new FileOutputStream("target/junior_registry_out.xlsx")) {
            exporter.exportJuniors(bundle, fileOut);
        }
    }

}
