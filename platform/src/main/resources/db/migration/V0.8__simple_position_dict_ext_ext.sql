IF NOT EXISTS (
  select *
  from   sys.columns
  where  object_id = OBJECT_ID(N'[dbo].[dict_position]')
         and name = 'category'
)
begin
  ALTER TABLE dict_position ADD category nvarchar(255) NULL;
end
GO

IF OBJECT_ID('v_employee_detailed', 'V') IS NOT NULL
drop view v_employee_detailed;
GO

create view v_employee_detailed as
        select e.*,
        d.name as department_name,
        l.name as level_name,
        p.name as position_name,
        p.category as position_category,
        project.name as current_project_name
        from employee e
        left join department d on e.department = d.id
        left join dict_level l on e.level = l.id
        left join dict_position p on e.position = p.id
        left join project project on e.current_project = project.id