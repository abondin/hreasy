-- Safe scripts with dicts test data

-- Departments
IF NOT EXISTS (SELECT id from dict.department where name='Development')
begin
    insert into department (name) values ('Development')
end

IF NOT EXISTS (SELECT id from dict.department where name='Integration')
begin
    insert into department (name) values ('Integration')
end

-- Level
IF NOT EXISTS (SELECT id from dict.level where name='Senior')
begin
    insert into dict.level (name) values ('Senior')
end
IF NOT EXISTS (SELECT id from dict.level where name='Junior')
begin
    insert into dict.level (name) values ('Junior')
end

-- Position
IF NOT EXISTS (SELECT id from dict.position where name='Java Developer')
begin
    insert into dict.position (name) values ('Java Developer')
end

IF NOT EXISTS (SELECT id from dict.position where name='Project Manager')
begin
    insert into dict.position (name) values ('Project Manager')
end

IF NOT EXISTS (SELECT id from dict.position where name='Head of Department')
begin
    insert into dict.position (name) values ('Head of Department')
end


IF NOT EXISTS (SELECT id from dict.position where name='Automation QA')
begin
    insert into dict.position (name) values ('Automation QA')
end


-- Projects
IF NOT EXISTS (SELECT id from dict.project where name='M1 Billing')
begin
    insert into dict.project (name, start_date, customer, department_id) values
      ('M1 Billing', GETDATE(), 'Mobile Operator N1', (SELECT id from dict.department where name='Development'))
end

IF NOT EXISTS (SELECT id from dict.project where name='M1 FMS')
begin
    insert into dict.project (name, start_date, customer, department_id) values
      ('M1 FMS', GETDATE(), 'Mobile Operator N1', (SELECT id from dict.department where name='Development'))
end

IF NOT EXISTS (SELECT id from dict.project where name='M1 Policy Manager')
begin
    insert into dict.project (name, start_date, customer, department_id) values
      ('M1 Policy Manager', GETDATE(), 'Mobile Operator N1', (SELECT id from dict.department where name='Development'))
end


IF NOT EXISTS (SELECT id from dict.project where name='M1 ERP Integration')
begin
    insert into dict.project (name, start_date, customer, department_id) values
      ('M1 ERP Integration', GETDATE(), 'Mobile Operator N1', (SELECT id from dict.department where name='Integration'))
end
