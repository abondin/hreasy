package ru.abondin.hreasy.platform.repo.udr;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

public interface JuniorRepo extends ReactiveCrudRepository<JuniorEntry, Integer> {
    String BASE_VIEW_QUERY = """
                select j.*, je.display_name as junior_display_name,
                        mentor.display_name as mentor_display_name,
                        created.display_name as created_by_display_name,
                        graduated.display_name as graduated_by_display_name,
                        ba.name as budgeting_account_name,
                        je.current_project as current_project_id,
                        project.name as current_project_name,
                        je.current_project_role as current_project_role,
                        (
                            SELECT json_agg(
                                json_build_object(
                                    'progress', report.progress,
                                    'comment', report.comment,
                                    'createdBy', json_build_object('id', created.id, 'name', created.display_name),
                                    'createdAt', report.created_at)
                                )
                            FROM udr.junior_report report
                            INNER JOIN empl.employee created ON report.created_by = created.id
                            WHERE report.junior_id = j.junior_id
                            AND report.deleted_at IS NULL
                         ) AS reports
                from udr.junior_registry j
                left join empl.employee je on j.junior_id = je.id
                left join empl.employee mentor on j.mentor_id = mentor.id
                left join empl.employee created on j.created_by = created.id
                left join empl.employee graduated on j.graduated_by = graduated.id
                left join ba.business_account  ba on j.budgeting_account = ba.id
                left join proj.project project on je.current_project = project.id
            """;
    String MENTOR_FILTER = """
            j.mentor_id is not null and (j.mentor_id = :mentorId or exists(
                select 1 from udr.junior_report jr where jr.junior_id = j.junior_id and jr.created_by = :mentorId and jr.deleted_at is null
            ))
            """;
    String PROJECTS_FILTER = """
            je.current_project in (:accessibleProjects)
             """;
    String BAS_FILTER = """
            j.budgeting_account in (:accessibleBas)
            or project.ba_id in (:accessibleBas)
            """;

    @Query(BASE_VIEW_QUERY)
    Flux<JuniorView> findAllDetailed();


    /**
     * Use findAllByBaProjectOrMentorSafe to avoid SQL errors
     * @param accessibleBas
     * @param accessibleProjects
     * @param mentorId
     * @return
     */
    @Query(BASE_VIEW_QUERY + " where (" + PROJECTS_FILTER + ")" +
            " or (" + BAS_FILTER + ")" +
            " or (" + MENTOR_FILTER + ")")
    Flux<JuniorView> notSafeFindAllByBaProjectOrMentor(List<Integer> accessibleBas,
                                                       List<Integer> accessibleProjects,
                                                       int mentorId);

    default Flux<JuniorView> findAllByBaProjectOrMentorSafe(List<Integer> accessibleBas,
                                                            List<Integer> accessibleProjects,
                                                            int mentorId) {
        return notSafeFindAllByBaProjectOrMentor(
                CollectionUtils.isEmpty(accessibleBas) ? Arrays.asList(Integer.MIN_VALUE) : accessibleBas,
                CollectionUtils.isEmpty(accessibleProjects) ? Arrays.asList(Integer.MIN_VALUE) : accessibleProjects,
                mentorId
        );
    }

}
