package ru.abondin.hreasy.platform.service.mapper;

import org.apache.commons.lang3.StringUtils;
import ru.abondin.hreasy.platform.service.dto.CurrentProjectDictDto;
import ru.abondin.hreasy.platform.service.dto.OfficeLocationDictDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public interface MapperBase {


    static YearMonth fromPeriodId(Integer periodId) {
        if (periodId == null) {
            return null;
        }
        return YearMonth.of(periodId / 100, periodId % 100 + 1);
    }

    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    default SimpleDictDto simpleDto(Integer id, String name) {
        return id == null ? null : new SimpleDictDto(id, name);
    }

    default CurrentProjectDictDto currentProjectDto(Integer id, String name, String role) {
        return id == null ? null : new CurrentProjectDictDto(id, name, role);
    }

    default OfficeLocationDictDto officeLocationDto(Integer id, String name, Integer officeId, String mapName) {
        return id == null ? null : new OfficeLocationDictDto(id, name, officeId, mapName);
    }


    default List<Integer> splitIds(String commaSeparatedIds) {
        return splitToStream(commaSeparatedIds)
                .map(Integer::valueOf).toList();
    }

    default List<String> splitStrings(String commaSeparatedStrings) {
        return splitToStream(commaSeparatedStrings)
                .map(String::trim).toList();

    }

    default Stream<String> splitToStream(String commaSeparatedStrings) {
        return Arrays.stream(
                StringUtils.isBlank(commaSeparatedStrings) ? new String[0] :
                        StringUtils.split(commaSeparatedStrings, ','));
    }
}
