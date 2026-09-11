package ru.abondin.hreasy.platform.repo.allocation;

import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationRepository.ResourceAllocationProjectView;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/** R2DBC queries for allocation-cell comments and annual comment counts. */
@Repository
@RequiredArgsConstructor
public class ResourceAllocationCommentRepository {
    private final DatabaseClient databaseClient;

    public Flux<CellSummaryView> findYearCellSummaries(int year) {
        return databaseClient.sql("""
                        select c.period, c.employee_id, c.project_id, c.workstream_id, c.comment_count,
                               e.display_name as employee_name, e.email as employee_email,
                               e.department as employee_department_id, ed.name as employee_department_name,
                               e.current_project as current_project_id, cp.name as current_project_name,
                               e.current_project_role,
                               p.name as project_name, p.department_id as project_department_id,
                               pd.name as project_department_name, p.ba_id as project_ba_id,
                               ba.name as project_ba_name, p.start_date, p.end_date,
                               w.display_name as workstream_name
                        from (
                            select period, employee_id, project_id, workstream_id, count(*)::integer as comment_count
                            from alloc.resource_allocation_comment
                            where year = :year
                            group by period, employee_id, project_id, workstream_id
                        ) c
                        join empl.employee e on e.id = c.employee_id
                        left join dict.department ed on ed.id = e.department
                        left join proj.project cp on cp.id = e.current_project
                        join proj.project p on p.id = c.project_id
                        left join dict.department pd on pd.id = p.department_id
                        left join ba.business_account ba on ba.id = p.ba_id
                        left join proj.project_workstream w on w.id = c.workstream_id
                        order by e.display_name, p.name, w.display_name nulls first, c.period
                        """)
                .bind("year", year)
                .map((row, _) -> new CellSummaryView(
                        row.get("period", Integer.class),
                        row.get("employee_id", Integer.class),
                        row.get("project_id", Integer.class),
                        row.get("workstream_id", Integer.class),
                        row.get("comment_count", Integer.class),
                        row.get("employee_name", String.class),
                        row.get("employee_email", String.class),
                        row.get("employee_department_id", Integer.class),
                        row.get("employee_department_name", String.class),
                        row.get("current_project_id", Integer.class),
                        row.get("current_project_name", String.class),
                        row.get("current_project_role", String.class),
                        new ResourceAllocationProjectView(
                                row.get("project_id", Integer.class),
                                row.get("project_name", String.class),
                                row.get("project_department_id", Integer.class),
                                row.get("project_department_name", String.class),
                                row.get("project_ba_id", Integer.class),
                                row.get("project_ba_name", String.class),
                                row.get("start_date", LocalDate.class),
                                row.get("end_date", LocalDate.class)),
                        row.get("workstream_name", String.class)))
                .all();
    }

    public Flux<CommentView> findCellComments(CellKey key) {
        var spec = databaseClient.sql(COMMENT_VIEW_SQL + """
                        where c.year = :year and c.period = :period and c.employee_id = :employeeId
                          and c.project_id = :projectId and c.workstream_id is not distinct from :workstreamId
                        order by c.created_at, c.id
                        """)
                .bind("year", key.year())
                .bind("period", key.period())
                .bind("employeeId", key.employeeId())
                .bind("projectId", key.projectId());
        return bindWorkstream(spec, key.workstreamId()).map(this::toCommentView).all();
    }

    public Mono<CommentView> findById(int id) {
        return databaseClient.sql(COMMENT_VIEW_SQL + " where c.id = :id")
                .bind("id", id)
                .map(this::toCommentView)
                .one();
    }

    public Mono<Integer> insert(CellKey key, String text, OffsetDateTime createdAt, int createdBy) {
        var spec = databaseClient.sql("""
                        insert into alloc.resource_allocation_comment
                            (year, period, employee_id, project_id, workstream_id, comment, created_at, created_by)
                        values (:year, :period, :employeeId, :projectId, :workstreamId, :comment, :createdAt, :createdBy)
                        returning id
                        """)
                .bind("year", key.year())
                .bind("period", key.period())
                .bind("employeeId", key.employeeId())
                .bind("projectId", key.projectId())
                .bind("comment", text)
                .bind("createdAt", createdAt)
                .bind("createdBy", createdBy);
        return bindWorkstream(spec, key.workstreamId())
                .map((row, _) -> row.get("id", Integer.class))
                .one();
    }

    public Mono<Long> update(int id, String text, OffsetDateTime updatedAt) {
        return databaseClient.sql("""
                        update alloc.resource_allocation_comment
                        set comment = :comment, updated_at = :updatedAt
                        where id = :id
                        """)
                .bind("id", id)
                .bind("comment", text)
                .bind("updatedAt", updatedAt)
                .fetch().rowsUpdated();
    }

    public Mono<Long> delete(int id) {
        return databaseClient.sql("delete from alloc.resource_allocation_comment where id = :id")
                .bind("id", id)
                .fetch().rowsUpdated();
    }

    private CommentView toCommentView(io.r2dbc.spi.Readable row) {
        return new CommentView(
                row.get("id", Integer.class),
                new CellKey(
                        row.get("year", Integer.class),
                        row.get("period", Integer.class),
                        row.get("employee_id", Integer.class),
                        row.get("project_id", Integer.class),
                        row.get("workstream_id", Integer.class)),
                row.get("comment", String.class),
                row.get("created_at", OffsetDateTime.class),
                row.get("created_by", Integer.class),
                row.get("updated_at", OffsetDateTime.class),
                row.get("author_name", String.class));
    }

    private DatabaseClient.GenericExecuteSpec bindWorkstream(DatabaseClient.GenericExecuteSpec spec,
                                                              Integer workstreamId) {
        return workstreamId == null
                ? spec.bindNull("workstreamId", Integer.class)
                : spec.bind("workstreamId", workstreamId);
    }

    private static final String COMMENT_VIEW_SQL = """
            select c.id, c.year, c.period, c.employee_id, c.project_id, c.workstream_id,
                   c.comment, c.created_at, c.created_by, c.updated_at,
                   author.display_name as author_name
            from alloc.resource_allocation_comment c
            join empl.employee author on author.id = c.created_by
            """;

    public record CellKey(int year, int period, Integer employeeId, Integer projectId, Integer workstreamId) {
    }

    public record CellSummaryView(
            Integer period,
            Integer employeeId,
            Integer projectId,
            Integer workstreamId,
            Integer commentCount,
            String employeeName,
            String employeeEmail,
            Integer employeeDepartmentId,
            String employeeDepartmentName,
            Integer currentProjectId,
            String currentProjectName,
            String currentProjectRole,
            ResourceAllocationProjectView project,
            String workstreamName) {
    }

    public record CommentView(
            Integer id,
            CellKey cell,
            String text,
            OffsetDateTime createdAt,
            Integer createdBy,
            OffsetDateTime updatedAt,
            String authorName) {
    }
}
