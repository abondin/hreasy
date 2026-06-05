CREATE SEQUENCE IF NOT EXISTS sal.salary_request_link_id_seq;
CREATE TABLE IF NOT EXISTS sal.salary_request_link (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('sal.salary_request_link_id_seq'),
    type integer NOT NULL,
    source integer NOT NULL REFERENCES sal.salary_request (id),
    destination integer NOT NULL REFERENCES sal.salary_request (id),
    comment text NULL,
    created_by integer NOT NULL REFERENCES empl.employee (id),
    created_at timestamp with time zone NOT NULL DEFAULT now(),
    deleted_by integer,
    deleted_at timestamp with time zone
);

CREATE UNIQUE INDEX salary_request_link_unique ON sal.salary_request_link (source, destination, type) WHERE (deleted_at is null);


COMMENT ON TABLE sal.salary_request_link IS 'Links between salary requests';
COMMENT ON COLUMN sal.salary_request_link.id IS 'Primary key';
COMMENT ON COLUMN sal.salary_request_link.type IS '
1 - Rescheduled. ''Rescheduled From'' for link Source, ''Rescheduled To'' for link Destination.
2 - Multi-Stage. ''Part of Multi-Stage Increase'' for all linked requests, ''Stage X of Y'' in description.
';
COMMENT ON COLUMN sal.salary_request_link.source IS 'Source request';
COMMENT ON COLUMN sal.salary_request_link.destination IS 'Destination request';
COMMENT ON COLUMN sal.salary_request_link.comment IS 'Comment';
COMMENT ON COLUMN sal.salary_request_link.created_by IS 'Created by (employee)';
COMMENT ON COLUMN sal.salary_request_link.created_at IS 'Created at';
COMMENT ON COLUMN sal.salary_request_link.deleted_by IS 'Deleted by (employee)';
COMMENT ON COLUMN sal.salary_request_link.deleted_at IS 'Deleted at';

-- Salary Request Full Information View with
-- DisplayName of createdBy
-- Merged approvals in column as a JSON array
-- Links in column as a JSON array
drop view sal.v_salary_request_full;
create view sal.v_salary_request_full AS
select
                r.*,
                e.display_name as employee_display_name,
                p.name info_empl_project_name,
                pos.name as info_empl_position_name,
                ba.name as info_empl_ba_name,
                budget_ba.name as budget_business_account_name,
                asm.planned_date as assessment_planned_date,
                cr.display_name as created_by_display_name,
                im.display_name as implemented_by_display_name,
                impl_new_pos.name as impl_new_position_name,
                req_new_pos.name as req_new_position_name,
                approvals.approval_data AS approvals,
                (
                    SELECT jsonb_agg(
                        jsonb_build_object(
                            'id', link.id,
                            'type', link.type,
                            'initiator',
                            CASE
                                WHEN link.source = r.id THEN true
                                ELSE false
                            END,
                            'linkedRequest',
                            jsonb_build_object(
                                'id', linked.id,
                                'period', linked.req_increase_start_period,
                                'implState', linked.impl_state,
                                'createdA', linked.created_at,
                                'createdBy', json_build_object(
                                    'id', linked.created_by,
                                     'name', (SELECT display_name as name FROM empl.employee WHERE id = linked.created_by)
                                )
                            ),
                            'comment', link.comment,
                            'createdAt', link.created_at,
                            'createdBy', json_build_object(
                                'id', link.created_by,
                                 'name', (SELECT display_name as name FROM empl.employee WHERE id = link.created_by)
                            )
                        )
                    )
                    from sal.salary_request_link link
                    left join sal.salary_request linked on
                              linked.id =
                                CASE
                                  WHEN link.source = r.id THEN link.destination
                                  ELSE link.source
                                  END
                    where link.deleted_at IS null and linked.deleted_at  is null
                    AND (link.source = r.id OR link.destination = r.id)
                ) AS links
            from sal.salary_request r
                left join empl.employee e on r.employee_id = e.id
                left join proj.project p on e.current_project=p.id
                left join dict.position pos on e.position = pos.id
                left join ba.business_account ba on r.budget_business_account=ba.id
                left join ba.business_account budget_ba on r.budget_business_account=budget_ba.id
                left join assmnt.assessment asm on r.assessment_id=asm.id
                left join empl.employee cr on r.created_by = cr.id
                left join empl.employee im on r.implemented_by = im.id
                left join dict.position impl_new_pos on r.impl_new_position =impl_new_pos.id
                left join dict.position req_new_pos on r.req_new_position = req_new_pos.id
                LEFT JOIN
                    (
                        SELECT
                            request_id,
                            json_agg(json_build_object(
                                'id', id,
                                'requestId', request_id,
                                'state', state,
                                'comment', comment,
                                'createdAt', created_at,
                                'createdBy', json_build_object(
                                    'id', created_by,
                                     'name', (SELECT display_name as name FROM empl.employee WHERE id = created_by)
                                )
                            )) AS approval_data
                        FROM
                            sal.salary_request_approval where deleted_at is null
                        GROUP BY
                            request_id
                    ) approvals ON r.id = approvals.request_id;


