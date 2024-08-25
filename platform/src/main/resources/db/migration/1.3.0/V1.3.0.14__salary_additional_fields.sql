ALTER TABLE sal.salary_request  ADD COLUMN IF NOT EXISTS
    req_new_position integer NULL REFERENCES dict.position (id);
ALTER TABLE sal.salary_request  ADD COLUMN IF NOT EXISTS
    info_previous_salary_increase_date date NULL;

COMMENT ON COLUMN sal.salary_request.req_new_position IS 'If new position requested';
COMMENT ON COLUMN sal.salary_request.impl_new_position IS 'If new position requested';
COMMENT ON COLUMN sal.salary_request.info_previous_salary_increase_date IS 'Date of previous implemented salary increase';

-- Salary Request Full Information View with displayName of createdBy and Merged approvals in column as a JSON array
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
                approvals.approval_data AS approvals
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


