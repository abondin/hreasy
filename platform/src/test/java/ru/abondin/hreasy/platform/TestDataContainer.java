package ru.abondin.hreasy.platform;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.api.employee.UpdateCurrentProjectBody;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Load ids of all test data (dicts and employees).
 * Initial DB scripts can be found in 'test/resources/db/testdata' folder
 * <p>
 * We have 16 employees
 *
 * <img src="file:{@docRoot}/db/testdata/testdata.png"/></img>
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


    private final Map<String, Integer> projects = new HashMap<>();
    private final Map<String, Integer> bas = new HashMap<>();
    public final Map<String, Integer> employees = new HashMap<>();


    /**
     * Does actual data loading
     *
     * @return
     */
    public Mono<?> initAsync() {
        this.projects.clear();
        this.employees.clear();
        this.bas.clear();
        return simpleDicts("proj.project", projects)
                .then(simpleDicts("ba.business_account", bas))
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
                        "select substring(email, 0, position('@' in email)) name, id from empl.employee")
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

    public UpdateCurrentProjectBody updateCurrentProjectBody(String projectName) {
        var id = projects.get(projectName);
        return id == null ? null : new UpdateCurrentProjectBody(id, "Tester");
    }


    public Integer project_M1_Billing() {
        return projects.get("M1 Billing");
    }

    public Integer project_M1_FMS() {
        return projects.get("M1 FMS");
    }

    public Integer project_M1_Policy_Manager() {
        return projects.get("M1 Policy Manager");
    }

    public Integer project_M1_ERP_Integration() {
        return projects.get("M1 ERP Integration");
    }

    public Integer ba_RND() {
        return bas.get("RND");
    }

    public Integer ba_Billing() {
        return bas.get("Billing");
    }
}
