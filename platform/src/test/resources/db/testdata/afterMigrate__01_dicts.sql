-- Safe scripts with dicts test data

-- Departments
insert into dict.department (name) 
 select  ('Development')
 where NOT EXISTS (SELECT id from dict.department where name='Development');

insert into dict.department (name) 
 select  ('Integration')
 where NOT EXISTS (SELECT id from dict.department where name='Integration');

-- Level
insert into dict.level (name) 
 select  ('Senior')
 where NOT EXISTS (SELECT id from dict.level where name='Senior');
insert into dict.level (name) 
 select  ('Junior')
 where NOT EXISTS (SELECT id from dict.level where name='Junior');

-- Position
insert into dict.position (name) 
 select  ('Java Developer')
 where NOT EXISTS (SELECT id from dict.position where name='Java Developer');

insert into dict.position (name) 
 select  ('Project Manager')
 where NOT EXISTS (SELECT id from dict.position where name='Project Manager');

insert into dict.position (name) 
 select  ('Head of Department')
 where NOT EXISTS (SELECT id from dict.position where name='Head of Department');


insert into dict.position (name) 
 select  ('Automation QA')
 where NOT EXISTS (SELECT id from dict.position where name='Automation QA');

-- BAs

INSERT INTO ba.business_account ("name") VALUES('RND');
INSERT INTO ba.business_account ("name") VALUES('Billing');



-- Projects
insert into proj.project (name, start_date, customer, department_id, ba_id)
 select 'M1 Billing', now()::date, 'Mobile Operator N1', (SELECT id from dict.department where name='Development')
 ,(SELECT id from ba.business_account where name='Billing')
 where NOT EXISTS (SELECT id from proj.project where name='M1 Billing');

insert into proj.project (name, start_date, customer, department_id, ba_id)
 select 'M1 FMS', now()::date, 'Mobile Operator N1', (SELECT id from dict.department where name='Development')
 ,(SELECT id from ba.business_account where name='RND')
 where NOT EXISTS (SELECT id from proj.project where name='M1 FMS');

insert into proj.project (name, start_date, customer, department_id, ba_id)
 select 'M1 Policy Manager', now()::date, 'Mobile Operator N1', (SELECT id from dict.department where name='Development')
 ,(SELECT id from ba.business_account where name='RND')
 where NOT EXISTS (SELECT id from proj.project where name='M1 Policy Manager');


insert into proj.project (name, start_date, customer, department_id) 
 select 'M1 ERP Integration', now()::date, 'Mobile Operator N1', (SELECT id from dict.department where name='Integration')
 where NOT EXISTS (SELECT id from proj.project where name='M1 ERP Integration');
