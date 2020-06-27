if not exists (select * from sysobjects where name='office_location' and xtype='U')
begin
create TABLE office_location (
	id int IDENTITY(1,1) PRIMARY KEY,
	name nvarchar(255) NOT NULL,
	description nvarchar(1024) NULL,
	office  nvarchar(255) NULL
)
end
GO


IF NOT EXISTS (
  select *
  from   sys.columns
  where  object_id = OBJECT_ID(N'[dbo].[employee]')
         and name = 'office_location'
)
begin
  ALTER TABLE employee ADD office_location int

  ALTER TABLE employee ADD CONSTRAINT employee_office_location_FK FOREIGN KEY (office_location) REFERENCES office_location(id)
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
        project.name as current_project_name,
        o.id as office_location_id,
        o.name as office_location_name
        from employee e
        left join department d on e.department = d.id
        left join dict_level l on e.level = l.id
        left join dict_position p on e.position = p.id
        left join project project on e.current_project = project.id
        left join office_location o on e.office_location = o.id