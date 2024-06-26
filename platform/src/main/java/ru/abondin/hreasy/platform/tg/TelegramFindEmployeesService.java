package ru.abondin.hreasy.platform.tg;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.config.HrEasyTelegramBotProps;
import ru.abondin.hreasy.platform.service.EmployeeService;
import ru.abondin.hreasy.platform.tg.dto.FindEmployeeRequest;
import ru.abondin.hreasy.platform.tg.dto.FindEmployeeResponse;
import ru.abondin.hreasy.platform.tg.dto.TgMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramFindEmployeesService {
    private final EmployeeService employeeService;
    private final TgMapper tgMapper;
    private final HrEasyTelegramBotProps props;

    public Mono<FindEmployeeResponse> find(AuthContext auth, @NotNull FindEmployeeRequest query, Locale locale) {
        log.info("find employees by {}: {}", auth.getUsername(), query);
        if (StringUtils.isBlank(query.query())) {
            return Mono.just(new FindEmployeeResponse(new ArrayList<>(), false));
        }
        var searchTerms = query.query().split("\\s+");
        LevenshteinDistance levenshteinDistance = LevenshteinDistance.getDefaultInstance();
        return employeeService.findAll(auth, false)
                .map(tgMapper::toFindEmployeeResponseEmployee)
                .map(e -> {
                    var score = score(employeeProps(e, locale), Stream.of(searchTerms).map(s -> s.toLowerCase(locale)).toList(), levenshteinDistance);
                    e.setScore(score);
                    return e;
                })
                .filter(e -> e.getScore() <= props.getFindEmployeeLevensteinThreshold())
                .collectList().map(employees -> {
                    var minScore = employees.stream().mapToInt(e -> e.getScore()).min().orElse(0);
                    var filtered = employees.stream().filter(e -> e.getScore() == minScore).toList();
                    var hasMore = filtered.size() > query.maxResults();
                    return new FindEmployeeResponse(hasMore ? filtered.subList(0, query.maxResults()) : filtered, hasMore);
                });
    }

    private List<String> employeeProps(FindEmployeeResponse.EmployeeDto employee, Locale locale) {
        var props = new ArrayList<String>();
        props.add(employee.getEmail().toLowerCase(locale));
        if (StringUtils.isNotBlank(employee.getDisplayName())) {
            props.addAll(Stream.of(employee.getDisplayName().split("\\s+")).map(s -> s.toLowerCase(locale)).toList());
        }
        if (StringUtils.isNotBlank(employee.getTelegram())) {
            props.add(employee.getTelegram().toLowerCase(locale));
        }
        if (StringUtils.isNotBlank(employee.getProjectName())) {
            props.add(employee.getProjectName().toLowerCase(locale));
        }
        if (StringUtils.isNotBlank(employee.getProjectRole())) {
            props.add(employee.getProjectRole().toLowerCase(locale));
        }
        return props;
    }

    private int score(List<String> employeeProps, List<String> searchTerms, LevenshteinDistance levenshteinDistance) {
        int totalScore = 0;
        for (var term : searchTerms) {
            int termScore = Integer.MAX_VALUE;
            for (var prop : employeeProps) {
                if (prop.equals(term)) {
                    termScore = 0;
                    break;
                } else if (prop.contains(term)) {
                    termScore = 1;
                    break;
                } else if (prop.length() >= term.length()) {
                    termScore = Math.min(termScore, levenshteinDistance.apply(prop, term));
                }
            }
            totalScore = Math.max(totalScore, termScore);
        }
        return totalScore;
    }

}
