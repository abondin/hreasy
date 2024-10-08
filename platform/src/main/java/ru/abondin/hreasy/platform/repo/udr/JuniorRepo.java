package ru.abondin.hreasy.platform.repo.udr;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

public interface JuniorRepo extends ReactiveCrudRepository<JuniorEntry, Integer> {
    String BASE_VIEW_QUERY = """
                select j.*, je.display_name as junior_empl_display_name,
                        je.date_of_employment as junior_date_of_employment,
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
                                    'id', report.id,
                                    'progress', report.progress,
                                    'comment', report.comment,
                                    'createdBy', json_build_object('id', created.id, 'name', created.display_name),
                                    'createdAt', report.created_at,
                                    'ratings', report.ratings) ORDER BY created_at desc
                                )
                            FROM udr.junior_report report
                            INNER JOIN empl.employee created ON report.created_by = created.id
                            WHERE report.junior_id = j.id
                            AND (report.deleted_at IS NULL or report.deleted_at >= :now)
                         ) AS reports
                from udr.junior_registry j
                left join empl.employee je on j.junior_empl_id = je.id
                left join empl.employee mentor on j.mentor_id = mentor.id
                left join empl.employee created on j.created_by = created.id
                left join empl.employee graduated on j.graduated_by = graduated.id
                left join ba.business_account  ba on j.budgeting_account = ba.id
                left join proj.project project on je.current_project = project.id
                where (j.deleted_at is null or j.deleted_at >= :now)
                    and (je.date_of_dismissal is null or je.date_of_dismissal >= :now)
                    and (j.junior_empl_id != :juniorEmployeeId)
            """;
    String MENTOR_FILTER = """
            j.mentor_id is not null and (j.mentor_id = :mentorId or exists(
                select 1 from udr.junior_report jr where jr.junior_id = j.id and jr.created_by = :mentorId and 
                (jr.deleted_at is null or jr.deleted_at>=:now) 
            ))
            """;
    String PROJECTS_FILTER = """
            je.current_project in (:accessibleProjects)
             """;
    String BAS_FILTER = """
            j.budgeting_account in (:accessibleBas)
            or project.ba_id in (:accessibleBas)
            """;
    String DEPARTMENT_FILTER = """
            je.department in (:accessibleDepartments)
            """;
    String DEFAULT_ORDER = " order by je.display_name asc";

    @Query(BASE_VIEW_QUERY + DEFAULT_ORDER)
    Flux<JuniorView> findAllDetailed(OffsetDateTime now, int juniorEmployeeId);

    @Query(BASE_VIEW_QUERY +
            " and ((:includedGraduated) or j.graduated_at is null or j.graduated_at>=:now) "
            + DEFAULT_ORDER)
    Flux<JuniorView> findForExport(OffsetDateTime now, int juniorEmployeeId, boolean includedGraduated);

    @Query(BASE_VIEW_QUERY + " and (j.id = :juniorRegistryId)" + DEFAULT_ORDER)
    Mono<JuniorView> findDetailed(int juniorRegistryId, OffsetDateTime now, int juniorEmployeeId);


    /**
     * Use findAllByBaProjectOrMentorSafe to avoid SQL errors
     *
     * @param accessibleBas
     * @param accessibleProjects
     * @param mentorId
     * @return
     */
    @Query(BASE_VIEW_QUERY + " and (" +
            " (" + PROJECTS_FILTER + ")" +
            " or (" + DEPARTMENT_FILTER + ")" +
            " or (" + BAS_FILTER + ")" +
            " or (" + MENTOR_FILTER + ")" +
            ")" + DEFAULT_ORDER)
    Flux<JuniorView> notSafeFindAllByBaProjectOrMentor(List<Integer> accessibleBas,
                                                       List<Integer> accessibleDepartments,
                                                       List<Integer> accessibleProjects,
                                                       int mentorId,
                                                       OffsetDateTime now,
                                                       int juniorEmployeeId
    );

    default Flux<JuniorView> findAllByBaProjectOrMentorSafe(List<Integer> accessibleBas,
                                                            List<Integer> accessibleDepartments,
                                                            List<Integer> accessibleProjects,
                                                            int mentorId,
                                                            OffsetDateTime now,
                                                            int juniorEmployeeId) {
        return notSafeFindAllByBaProjectOrMentor(
                CollectionUtils.isEmpty(accessibleBas) ? Arrays.asList(Integer.MIN_VALUE) : accessibleBas,
                CollectionUtils.isEmpty(accessibleDepartments) ? Arrays.asList(Integer.MIN_VALUE) : accessibleDepartments,
                CollectionUtils.isEmpty(accessibleProjects) ? Arrays.asList(Integer.MIN_VALUE) : accessibleProjects,
                mentorId, now, juniorEmployeeId
        );
    }


    /**
     * Use findAllByBaProjectOrMentorSafe to avoid SQL errors
     *
     * @param accessibleBas
     * @param accessibleProjects
     * @param mentorId
     * @return
     */
    @Query(BASE_VIEW_QUERY + " and j.id = :juniorRegistryId and (" +
            " (" + PROJECTS_FILTER + ")" +
            " or (" + DEPARTMENT_FILTER + ")" +
            " or (" + BAS_FILTER + ")" +
            " or (" + MENTOR_FILTER + ")" +
            ")" + DEFAULT_ORDER)
    Mono<JuniorView> notSafeFindDetailedByBaProjectOrMentor(int juniorRegistryId,
                                                            List<Integer> accessibleBas,
                                                            List<Integer> accessibleDepartments,
                                                            List<Integer> accessibleProjects,
                                                            int mentorId,
                                                            OffsetDateTime now,
                                                            int juniorEmployeeId
    );

    default Mono<JuniorView> findDetailedByBaProjectOrMentorSafe(int juniorRegistryId,
                                                                 List<Integer> accessibleBas,
                                                                 List<Integer> accessibleDepartments,
                                                                 List<Integer> accessibleProjects,
                                                                 Integer mentorId,
                                                                 OffsetDateTime now,
                                                                 int juniorEmployeeId) {
        return notSafeFindDetailedByBaProjectOrMentor(
                juniorRegistryId,
                CollectionUtils.isEmpty(accessibleBas) ? Arrays.asList(Integer.MIN_VALUE) : accessibleBas,
                CollectionUtils.isEmpty(accessibleDepartments) ? Arrays.asList(Integer.MIN_VALUE) : accessibleDepartments,
                CollectionUtils.isEmpty(accessibleProjects) ? Arrays.asList(Integer.MIN_VALUE) : accessibleProjects,
                mentorId, now, juniorEmployeeId
        );
    }
}
