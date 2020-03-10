IF NOT EXISTS (
  select *
  from   sys.columns
  where  object_id = OBJECT_ID(N'[dbo].[employee]')
         and name = 'current_project'
)
begin
  ALTER TABLE employee ADD current_project int

  ALTER TABLE employee ADD CONSTRAINT employee_cur_project_FK FOREIGN KEY (current_project) REFERENCES project(id)
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
        project.name as current_project_name
        from employee e
        left join department d on e.department = d.id
        left join dict_level l on e.level = l.id
        left join dict_position p on e.position = p.id
        left join project project on e.current_project = project.id