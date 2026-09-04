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
                               e.current_project as current_project_id, p.name as current_project_name,
                               e.date_of_employment, e.date_of_dismissal
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
                        row.get("current_project_name", String.class),
                        row.get("date_of_employment", LocalDate.class),
                        row.get("date_of_dismissal", LocalDate.class)))
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
    public Flux<ResourceAllocationView> findAllocations(int year, int period) {
        return dbTemplate.getDatabaseClient().sql("""
                        select employee_id, project_id, percent, revision_id
                        from alloc.resource_allocation
                        where year = :year and period = :period
                        """)
                .bind("year", year)
                .bind("period", period)
                .map((row, _) -> new ResourceAllocationView(
                        row.get("employee_id", Integer.class),
                        row.get("project_id", Integer.class),
                        row.get("percent", Short.class).intValue(),
                        row.get("revision_id", Integer.class)))
                .all();
    }

    /**
     * Returns projects with allocation values in the requested calendar year.
     */
    public Flux<Integer> findAllocatedProjectIds(int year) {
        return dbTemplate.getDatabaseClient().sql("""
                        select distinct project_id
                        from alloc.resource_allocation
                        where year = :year
                        """)
                .bind("year", year)
                .map((row, _) -> row.get("project_id", Integer.class))
                .all();
    }

    /**
     * Returns all non-zero allocation cells for one calendar year.
     */
    public Flux<PeriodResourceAllocationView> findYearAllocations(int year) {
        return dbTemplate.getDatabaseClient().sql("""
                        select period, employee_id, project_id, percent, revision_id
                        from alloc.resource_allocation
                        where year = :year
                        order by period, employee_id, project_id
                        """)
                .bind("year", year)
                .map((row, _) -> new PeriodResourceAllocationView(
                        row.get("period", Integer.class),
                        row.get("employee_id", Integer.class),
                        row.get("project_id", Integer.class),
                        row.get("percent", Short.class).intValue(),
                        row.get("revision_id", Integer.class)))
                .all();
    }

    /**
     * Returns all monthly allocation values for one project and calendar year.
     */
    public Flux<PeriodResourceAllocationView> findProjectAllocations(int projectId, int year) {
        return dbTemplate.getDatabaseClient().sql("""
                        select period, employee_id, project_id, percent, revision_id
                        from alloc.resource_allocation
                        where project_id = :projectId and year = :year
                        """)
                .bind("projectId", projectId)
                .bind("year", year)
                .map((row, _) -> new PeriodResourceAllocationView(
                        row.get("period", Integer.class),
                        row.get("employee_id", Integer.class),
                        row.get("project_id", Integer.class),
                        row.get("percent", Short.class).intValue(),
                        row.get("revision_id", Integer.class)))
                .all();
    }

    /**
     * Creates the parent revision for one Save operation.
     */
    public Mono<Integer> createRevision(int year, int projectId, OffsetDateTime createdAt, int createdBy) {
        return dbTemplate.getDatabaseClient().sql("""
                        insert into alloc.resource_allocation_revision (year, project_id, created_at, created_by)
                        values (:year, :projectId, :createdAt, :createdBy)
                        returning id
                        """)
                .bind("year", year)
                .bind("projectId", projectId)
                .bind("createdAt", createdAt)
                .bind("createdBy", createdBy)
                .map((row, _) -> row.get("id", Integer.class))
                .one();
    }

    /**
     * Appends an immutable before/after value to a revision.
     */
    public Mono<Long> recordChange(int revisionId, int period, int employeeId,
                                   int previousPercent, int newPercent) {
        return dbTemplate.getDatabaseClient().sql("""
                        insert into alloc.resource_allocation_change
                            (revision_id, period, employee_id, previous_percent, new_percent)
                        values (:revisionId, :period, :employeeId, :previousPercent, :newPercent)
                        """)
                .bind("revisionId", revisionId)
                .bind("period", period)
                .bind("employeeId", employeeId)
                .bind("previousPercent", previousPercent)
                .bind("newPercent", newPercent)
                .fetch().rowsUpdated();
    }

    /**
     * Inserts a cell only when it is still absent.
     */
    public Mono<Long> insertIfAbsent(int year, int period, int employeeId, int projectId, int percent, int revisionId) {
        return dbTemplate.getDatabaseClient().sql("""
                        insert into alloc.resource_allocation
                            (year, period, employee_id, project_id, percent, revision_id)
                        values (:year, :period, :employeeId, :projectId, :percent, :revisionId)
                        on conflict (year, period, employee_id, project_id) do nothing
                        """)
                .bind("year", year)
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
    public Mono<Long> updateIfRevisionMatches(int year, int period, int employeeId, int projectId, int percent,
                                              int revisionId, int expectedRevisionId) {
        return dbTemplate.getDatabaseClient().sql("""
                        update alloc.resource_allocation
                        set percent = :percent, revision_id = :revisionId
                        where year = :year and period = :period and employee_id = :employeeId and project_id = :projectId
                          and revision_id = :expectedRevisionId
                        """)
                .bind("year", year)
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
    public Mono<Long> deleteIfRevisionMatches(int year, int period, int employeeId, int projectId,
                                              int expectedRevisionId) {
        return dbTemplate.getDatabaseClient().sql("""
                        delete from alloc.resource_allocation
                        where year = :year and period = :period and employee_id = :employeeId and project_id = :projectId
                          and revision_id = :expectedRevisionId
                        """)
                .bind("year", year)
                .bind("period", period)
                .bind("employeeId", employeeId)
                .bind("projectId", projectId)
                .bind("expectedRevisionId", expectedRevisionId)
                .fetch().rowsUpdated();
    }

    /**
     * Returns closed monthly periods for the requested calendar year.
     */
    public Flux<Integer> findClosedPeriods(int year) {
        return dbTemplate.getDatabaseClient().sql("""
                        select period
                        from alloc.resource_allocation_closed_period
                        where year = :year
                        order by period
                        """)
                .bind("year", year)
                .map((row, _) -> row.get("period", Integer.class))
                .all();
    }

    /**
     * Closes a monthly period unless it is already closed.
     */
    public Mono<Long> closePeriod(int year, int period, OffsetDateTime closedAt, int closedBy, String comment) {
        var spec = dbTemplate.getDatabaseClient().sql("""
                        insert into alloc.resource_allocation_closed_period
                            (year, period, closed_at, closed_by, comment)
                        values (:year, :period, :closedAt, :closedBy, :comment)
                        on conflict (period) do nothing
                        """)
                .bind("year", year)
                .bind("period", period)
                .bind("closedAt", closedAt)
                .bind("closedBy", closedBy);
        return (comment == null ? spec.bindNull("comment", String.class) : spec.bind("comment", comment))
                .fetch().rowsUpdated();
    }

    /**
     * Reopens a monthly period.
     */
    public Mono<Long> reopenPeriod(int period) {
        return dbTemplate.getDatabaseClient().sql("""
                        delete from alloc.resource_allocation_closed_period where period = :period
                        """)
                .bind("period", period)
                .fetch().rowsUpdated();
    }

    public record ResourceAllocationEmployeeView(Integer id, String displayName,
                                                 Integer departmentId, String departmentName,
                                                 Integer currentProjectId, String currentProjectName,
                                                 LocalDate dateOfEmployment, LocalDate dateOfDismissal) {
    }

    public record ResourceAllocationProjectView(Integer id, String name,
                                                Integer departmentId, String departmentName,
                                                Integer baId, String baName,
                                                LocalDate startDate, LocalDate endDate) {
    }

    public record ResourceAllocationView(Integer employeeId, Integer projectId,
                                         int percent, Integer revisionId) {
    }

    public record PeriodResourceAllocationView(Integer period, Integer employeeId, Integer projectId,
                                               int percent, Integer revisionId) {
    }
}
