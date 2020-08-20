if not exists (select * from sec_role where role='pm')
begin
    insert into sec_role(role, description) values ('pm', 'Project Manager')
    insert into sec_role_perm(role, permission) values ('pm', 'overtime_view')
    insert into sec_role_perm(role, permission) values ('pm', 'overtime_edit')
    insert into sec_role_perm(role, permission) values ('pm', 'vacation_view')
    insert into sec_role_perm(role, permission) values ('pm', 'vacation_edit')
end

IF OBJECT_ID('v_employee_detailed', 'V') IS NOT NULL
drop view v_employee_detailed;
GO

-- Add security user information to main employee view

create view v_employee_detailed as
        select e.*,
        d.name as department_name,
        l.name as level_name,
        p.name as position_name,
        p.category as position_category,
        project.name as current_project_name,
        o.id as office_location_id,
        o.name as office_location_name,
        us.id as sec_user_id
        from employee e
        left join department d on e.department = d.id
        left join dict_level l on e.level = l.id
        left join dict_position p on e.position = p.id
        left join project project on e.current_project = project.id
        left join office_location o on e.office_location = o.id
        left join sec_user us on e.id = us.employee_id
GO