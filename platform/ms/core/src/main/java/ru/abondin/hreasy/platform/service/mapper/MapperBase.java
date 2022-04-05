package ru.abondin.hreasy.platform.service.mapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface MapperBase {

    DateTimeFormatter DATE_FORMATTER=DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    default SimpleDictDto simpleDto(Integer id, String name) {
        return id == null ? null : new SimpleDictDto(id, name);
    }

    default List<Integer> splitIds(String commaSeparatedIds) {
        return splitToStream(commaSeparatedIds)
                .map(Integer::valueOf).collect(Collectors.toList());
    }

    default List<String> splitStrings(String commaSeparatedStrings) {
        return splitToStream(commaSeparatedStrings)
                .map(String::trim).collect(Collectors.toList());

    }

    default Stream<String> splitToStream(String commaSeparatedStrings) {
        return Arrays.stream(
                Strings.isBlank(commaSeparatedStrings) ? new String[0] :
                        StringUtils.split(commaSeparatedStrings, ','));
    }
}
