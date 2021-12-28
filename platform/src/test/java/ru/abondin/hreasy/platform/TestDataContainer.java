package ru.abondin.hreasy.platform;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Load ids of all test data (dicts and employees).
 *
 * <p>
 * <b>Do not forgot to call async init method in your test</b>
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TestDataContainer {

    private final R2dbcEntityTemplate db;


    public final Map<String, Integer> projects = new HashMap<>();
    public final Map<String, Integer> employees = new HashMap<>();


    /**
     * Does actual data loading
     *
     * @return
     */
    public Mono<?> initAsync() {
        this.projects.clear();
        this.employees.clear();
        return simpleDicts("proj.project", projects)
                .then(employees());
    }

    private Mono<?> simpleDicts(String dictTableName, Map<String, Integer> dictMap) {
        return db.getDatabaseClient().sql("select name, id from " + dictTableName)
                .fetch()
                .all()
                .doOnNext(d -> dictMap.put((String) d.get("name"), (Integer) d.get("id"))).collectList();
    }

    private Mono<?> employees() {
        return db.getDatabaseClient().sql(
                        "select CONCAT(firstname, '.', lastname) name, id from empl.employee")
                .fetch()
                .all()
                .doOnNext(d -> employees.put((String) d.get("name"), (Integer) d.get("id"))).collectList();
    }

    /**
     * We allow user to populate username without email suffix (@company.org)
     * In that case we append email suffix automatically
     * <p>
     * Username always converts to lowercase
     *
     * @param username
     * @return
     */
    public static String emailFromUserName(String username) {
        var result = username;
        if (Strings.isNotBlank("@stm-labs.ru")
                && Strings.isNotBlank(username) && !username.contains("@")) {
            result = username + "@stm-labs.ru";
        }
        return StringUtils.trimToEmpty(result).toLowerCase(Locale.ROOT);
    }
}
