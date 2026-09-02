package ru.abondin.hreasy.platform.repo.allocation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Executes allocation matrix queries and transactional revision writes that are not ordinary CRUD operations.
 */
@Repository
@RequiredArgsConstructor
public class ResourceAllocationRepository {
    private final R2dbcEntityTemplate dbTemplate;

    /**
     * Serializes allocation saves for one period inside the current transaction.
     */
    public Mono<Void> lockPeriod(int period) {
        return dbTemplate.getDatabaseClient().sql("select pg_advisory_xact_lock(712011, :period)")
                .bind("period", period)
                .fetch().one()
                .then();
    }

    /**
     * Finds employees whose employment overlaps the requested month.
     */
    public Flux<ResourceAllocationEmployeeView> findEmployees(LocalDate periodStart, LocalDate periodEnd) {
        return dbTemplate.getDatabaseClient().sql("""
                        select e.id, e.display_name, e.department as department_id, d.name as department_name,
                               e.current_project as current_project_id, p.name as current_project_name
                        from empl.employee e
                        left join dict.department d on d.id = e.department
                        left join proj.project p on p.id = e.current_project
                        where (e.date_of_employment is null or e.date_of_employment <= :periodEnd)
                          and (e.date_of_dismissal is null or e.date_of_dismissal >= :periodStart)
                        order by e.display_name
                        """)
                .bind("periodStart", periodStart)
                .bind("periodEnd", periodEnd)
                .map((row, _) -> new ResourceAllocationEmployeeView(
                        row.get("id", Integer.class),
                        row.get("display_name", String.class),
                        row.get("department_id", Integer.class),
                        row.get("department_name", String.class),
                        row.get("current_project_id", Integer.class),
                        row.get("current_project_name", String.class)))
                .all();
    }

    /**
     * Returns all projects required to render both editable and read-only allocation columns.
     */
    public Flux<ResourceAllocationProjectView> findProjects() {
        return dbTemplate.getDatabaseClient().sql("""
                        select p.id, p.name, p.department_id, d.name as department_name,
                               p.ba_id, ba.name as ba_name, p.start_date, p.end_date
                        from proj.project p
                        left join dict.department d on d.id = p.department_id
                        left join ba.business_account ba on ba.id = p.ba_id
                        order by p.name
                        """)
                .map((row, _) -> new ResourceAllocationProjectView(
                        row.get("id", Integer.class),
                        row.get("name", String.class),
                        row.get("department_id", Integer.class),
                        row.get("department_name", String.class),
                        row.get("ba_id", Integer.class),
                        row.get("ba_name", String.class),
                        row.get("start_date", LocalDate.class),
                        row.get("end_date", LocalDate.class)))
                .all();
    }

    /**
     * Returns the current non-zero allocation cells for a period.
     */
    public Flux<ResourceAllocationView> findAllocations(int period) {
        return dbTemplate.getDatabaseClient().sql("""
                        select employee_id, project_id, percent, revision_id
                        from alloc.resource_allocation
                        where period = :period
                        """)
                .bind("period", period)
                .map((row, _) -> new ResourceAllocationView(
                        row.get("employee_id", Integer.class),
                        row.get("project_id", Integer.class),
                        row.get("percent", Short.class).intValue(),
                        row.get("revision_id", Integer.class)))
                .all();
    }

    /**
     * Creates the parent revision for one Save operation.
     */
    public Mono<Integer> createRevision(int period, OffsetDateTime createdAt, int createdBy) {
        return dbTemplate.getDatabaseClient().sql("""
                        insert into alloc.resource_allocation_revision (period, created_at, created_by)
                        values (:period, :createdAt, :createdBy)
                        returning id
                        """)
                .bind("period", period)
                .bind("createdAt", createdAt)
                .bind("createdBy", createdBy)
                .map((row, _) -> row.get("id", Integer.class))
                .one();
    }

    /**
     * Appends an immutable before/after value to a revision.
     */
    public Mono<Long> recordChange(int revisionId, int employeeId, int projectId,
                                   int previousPercent, int newPercent) {
        return dbTemplate.getDatabaseClient().sql("""
                        insert into alloc.resource_allocation_change
                            (revision_id, employee_id, project_id, previous_percent, new_percent)
                        values (:revisionId, :employeeId, :projectId, :previousPercent, :newPercent)
                        """)
                .bind("revisionId", revisionId)
                .bind("employeeId", employeeId)
                .bind("projectId", projectId)
                .bind("previousPercent", previousPercent)
                .bind("newPercent", newPercent)
                .fetch().rowsUpdated();
    }

    /**
     * Inserts a cell only when it is still absent.
     */
    public Mono<Long> insertIfAbsent(int period, int employeeId, int projectId, int percent, int revisionId) {
        return dbTemplate.getDatabaseClient().sql("""
                        insert into alloc.resource_allocation
                            (period, employee_id, project_id, percent, revision_id)
                        values (:period, :employeeId, :projectId, :percent, :revisionId)
                        on conflict (period, employee_id, project_id) do nothing
                        """)
                .bind("period", period)
                .bind("employeeId", employeeId)
                .bind("projectId", projectId)
                .bind("percent", percent)
                .bind("revisionId", revisionId)
                .fetch().rowsUpdated();
    }

    /**
     * Replaces a cell only when it still has the revision seen by the client.
     */
    public Mono<Long> updateIfRevisionMatches(int period, int employeeId, int projectId, int percent,
                                              int revisionId, int expectedRevisionId) {
        return dbTemplate.getDatabaseClient().sql("""
                        update alloc.resource_allocation
                        set percent = :percent, revision_id = :revisionId
                        where period = :period and employee_id = :employeeId and project_id = :projectId
                          and revision_id = :expectedRevisionId
                        """)
                .bind("period", period)
                .bind("employeeId", employeeId)
                .bind("projectId", projectId)
                .bind("percent", percent)
                .bind("revisionId", revisionId)
                .bind("expectedRevisionId", expectedRevisionId)
                .fetch().rowsUpdated();
    }

    /**
     * Removes a current allocation cell while its deletion remains recorded in the revision.
     */
    public Mono<Long> deleteIfRevisionMatches(int period, int employeeId, int projectId, int expectedRevisionId) {
        return dbTemplate.getDatabaseClient().sql("""
                        delete from alloc.resource_allocation
                        where period = :period and employee_id = :employeeId and project_id = :projectId
                          and revision_id = :expectedRevisionId
                        """)
                .bind("period", period)
                .bind("employeeId", employeeId)
                .bind("projectId", projectId)
                .bind("expectedRevisionId", expectedRevisionId)
                .fetch().rowsUpdated();
    }

    public record ResourceAllocationEmployeeView(Integer id, String displayName,
                                                 Integer departmentId, String departmentName,
                                                 Integer currentProjectId, String currentProjectName) {
    }

    public record ResourceAllocationProjectView(Integer id, String name,
                                                Integer departmentId, String departmentName,
                                                Integer baId, String baName,
                                                LocalDate startDate, LocalDate endDate) {
    }

    public record ResourceAllocationView(Integer employeeId, Integer projectId,
                                         int percent, Integer revisionId) {
    }
}
