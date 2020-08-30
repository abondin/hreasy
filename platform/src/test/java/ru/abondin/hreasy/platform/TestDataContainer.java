package ru.abondin.hreasy.platform;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.repo.SimpleDictEntry;

import java.util.HashMap;
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

    private final DatabaseClient db;


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
        return simpleDicts("project", projects)
                .then(employees());
    }

    private Mono<?> simpleDicts(String dictTableName, Map<String, Integer> dictMap) {
        return db.execute("select name, id from " + dictTableName).as(SimpleDictEntry.class)
                .fetch().all()
                .doOnNext(d -> dictMap.put(d.getName(), d.getId())).collectList();
    }

    private Mono<?> employees() {
        return db.execute("select CONCAT(firstname, '.', lastname) name, id from employee").as(SimpleDictEntry.class)
                .fetch().all()
                .doOnNext(d -> employees.put(d.getName(), d.getId())).collectList();
    }
}
