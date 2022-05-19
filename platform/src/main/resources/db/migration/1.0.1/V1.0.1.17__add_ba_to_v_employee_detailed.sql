-- Add business account ID and name
create or replace view empl.v_employee_detailed as select e.*,
        d.name as department_name,
        l.name as level_name,
        p.name as position_name,
        p.category as position_category,
        project.name as current_project_name,
        o.id as office_location_id,
        o.name as office_location_name,
        sk.rating as aggregated_skills,
        ba.id as ba_id,
        ba.name as ba_name
        from empl.employee e
        left join dict.department d on e.department = d.id
        left join dict.level l on e.level = l.id
        left join dict.position p on e.position = p.id
        left join proj.project project on e.current_project = project.id
        left join ba.business_account ba on project.ba_id = ba.id
        left join dict.office_location o on e.office_location = o.id
        left join (
            select sr.employee_id, STRING_AGG(CONCAT_WS( '|#|',sr.id, sr.name, gr.id, gr.name, sr.str), '|$|') rating from (
                                      select s.employee_id, s.group_id, s.id, s.name, STRING_AGG(CONCAT_WS(',', r.created_by, r.rating), '/') str
                                       from empl.skill s left join empl.skill_rating r  on s.id=r.skill_id and r.deleted_at is null
                                        where s.deleted_at is null
                                    	group by s.employee_id, s.group_id, s.id, s.name
                                    ) sr  inner join empl.skill_group gr on gr.id=sr.group_id group by sr.employee_id

        ) sk
 on e.id = sk.employee_id;