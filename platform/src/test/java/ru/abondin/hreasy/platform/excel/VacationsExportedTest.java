package ru.abondin.hreasy.platform.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.vacation.VacationsExcelExporter;
import ru.abondin.hreasy.platform.service.vacation.dto.VacationDto;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class VacationsExportedTest {

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


    private VacationsExcelExporter.VacationsExportBundle bundle;


    private VacationsExcelExporter exporter;

    @BeforeEach
    public void before() {
        var vacations = new ArrayList<VacationDto>();
        int size = 100;
        var currentYear = LocalDate.now().getYear();
        for (int i = 1; i <= size; i++) {
            var lastname = lastNames.get((int) (Math.random() * lastNames.size()));
            var firstame = firstNames.get((int) (Math.random() * firstNames.size()));
            var patronicname = patronicNames.get((int) (Math.random() * patronicNames.size()));
            var displayName = Strings.join(Arrays.asList(lastname, firstame, patronicname), ' ');

            var vacation = new VacationDto();
            vacation.setEmployeeDisplayName(displayName);
            vacation.setDaysNumber((int) (1 + Math.random() * 30));
            vacation.setYear(currentYear + (int) (-2 + Math.random() * 2));
            vacation.setEmployeeCurrentProject(projects.get((int) (Math.random() * projects.size())));
            vacation.setDocuments(Math.random() > 0.5 ? "Документик" : null);
            vacation.setNotes(Math.random() > 0.5 ? "Примечание" : "");
            vacation.setStartDate(LocalDate.of(vacation.getYear(), (int) (1+Math.random() * 11), (int) (1 + Math.random() * 20)));
            vacation.setEndDate(vacation.getStartDate().plusDays(vacation.getDaysNumber()));
            vacation.setStatus(VacationDto.VacationStatus.values()[(int) (Math.random() * VacationDto.VacationStatus.values().length)]);
            vacations.add(vacation);
        }
        this.bundle = VacationsExcelExporter.VacationsExportBundle.builder()
                .exportTime(OffsetDateTime.now())
                .vacations(vacations)
                .years(new int[]{currentYear - 2, currentYear - 1, currentYear})
                .build();

        exporter = new VacationsExcelExporter((code, args) -> code);
    }

    private static float roundToHalf(double d) {
        return (float) (Math.round(d * 2) / 2.0);
    }

    @Test
    @Disabled("Nothing to tests yet. It is only to play with apache poi")
    public void testVacationExcelGeneration() throws Exception {
        var destination = File.createTempFile("simpletable", ".xlsx");
        log.debug("Writing test file to {}", destination);
        // Save
        try (var fileOut = new FileOutputStream(destination)) {
            exporter.exportVacations(bundle, fileOut);
        }

    }


}
