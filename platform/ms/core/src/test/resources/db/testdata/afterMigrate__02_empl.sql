-- Safe scripts with empl test data

-- Java Developer from M1 Billing
INSERT INTO empl.employee (email, lastname,firstname,patronymic_name,
  department,position,level,current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) select
  'Haiden.Spooner@stm-labs.ru',
  'Spooner', 'Haiden',null,
  (select id from dict.department  where name='Development' limit 1),
  (select id from dict.position  where name='Java Developer' limit 1),
  (select id from dict.level  where name='Senior' limit 1),
  (select id from proj.project  where name='M1 Billing' limit 1),
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'male',
  '2011-10-01 00:00:00.000',
  null,
  null where NOT EXISTS (SELECT id from empl.employee where email ilike 'Haiden.Spooner@stm-labs.ru');

-- Java Developer from M1 Billing
INSERT INTO empl.employee (email, lastname,firstname,patronymic_name,
  department,position,level,current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) select
  'Asiyah.Bob@stm-labs.ru',
  'Bob', 'Asiyah',null,
  (select id from dict.department  where name='Development' limit 1),
  (select id from dict.position  where name='Java Developer' limit 1),
  (select id from dict.level  where name='Senior' limit 1),
  (select id from proj.project  where name='M1 Billing' limit 1),
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'male',
  '2011-10-01 00:00:00.000',
  null,
  null where NOT EXISTS (SELECT id from empl.employee where email ilike 'Asiyah.Bob@stm-labs.ru');

-- Fired Java Developer from M1 Billing
INSERT INTO empl.employee (email, lastname,firstname,patronymic_name,
  department,position,level,current_project, phone, birthday,sex,date_of_employment,
  office_location,ext_erp_id, date_of_dismissal) select
  'Dev.Fired@stm-labs.ru',
  'Fired', 'Dev',null,
  (select id from dict.department  where name='Development' limit 1),
  (select id from dict.position  where name='Java Developer' limit 1),
  (select id from dict.level  where name='Senior' limit 1),
  (select id from proj.project  where name='M1 Billing' limit 1),
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'male',
  '2011-10-01 00:00:00.000',
  null,
  null,
  '2000-06-12 00:00:00.000' where NOT EXISTS (SELECT id from empl.employee where email ilike 'Dev.Fired@stm-labs.ru');

-- M1 Billing Project Lead
DO
$do$
BEGIN
IF NOT EXISTS (SELECT id from empl.employee where email ilike 'Maxwell.May@stm-labs.ru') then
INSERT INTO empl.employee (email, lastname,firstname,patronymic_name,
  department,position,level,current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) VALUES
(
  'Maxwell.May@stm-labs.ru',
  'May', 'Maxwell',null,
  (select id from dict.department  where name='Development' limit 1),
  (select id from dict.position  where name='Project Manager' limit 1),
  (select id from dict.level  where name='Senior' limit 1),
  (select id from proj.project  where name='M1 Billing' limit 1),
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'male',
  '2011-10-01 00:00:00.000',
  null,
  null);
INSERT INTO sec.employee_accessible_projects (employee_id, project_id) values (
(select id from empl.employee  where email ilike 'Maxwell.May@stm-labs.ru' limit 1),
(select id from proj.project  where name='M1 Billing' limit 1)
);
INSERT INTO sec.user_role (employee_id, role) values (
(select id from empl.employee  where email ilike 'Maxwell.May@stm-labs.ru' limit 1),
'pm'
);

end if;
END
$do$;

-- Development Department Lead
DO
$do$
BEGIN
IF NOT EXISTS (SELECT id from empl.employee where email ilike 'Percy.Gough@stm-labs.ru') then
INSERT INTO empl.employee (email, lastname,firstname,patronymic_name,
  department,position,level,current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) VALUES
(
  'Percy.Gough@stm-labs.ru',
  'Gough', 'Percy',null,
  (select id from dict.department  where name='Development' limit 1),
  (select id from dict.position  where name='Head of Department' limit 1),
  (select id from dict.level  where name='Senior' limit 1),
  null,
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'male',
  '2011-10-01 00:00:00.000',
  null,
  null);
INSERT into sec.employee_accessible_departments (employee_id, department_id) values (
(select id from empl.employee  where email ilike 'Percy.Gough@stm-labs.ru' limit 1),
(select id from dict.department  where name='Development' limit 1)
);
INSERT INTO sec.user_role (employee_id, role) values (
(select id from empl.employee  where email ilike 'Percy.Gough@stm-labs.ru' limit 1),
'pm'
);
end if;
END
$do$;

-- Java Developer from M1 FMS
DO
$do$
BEGIN
IF NOT EXISTS (SELECT id from empl.employee where email ilike 'Ammara.Knott@stm-labs.ru') then
INSERT INTO empl.employee (email, lastname,firstname,patronymic_name,
  department,position,level,current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) VALUES
(
  'Ammara.Knott@stm-labs.ru',
  'Knott', 'Ammara',null,
  (select id from dict.department  where name='Development' limit 1),
  (select id from dict.position  where name='Java Developer' limit 1),
  (select id from dict.level  where name='Senior' limit 1),
  (select id from proj.project  where name='M1 FMS' limit 1),
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'female',
  '2011-10-01 00:00:00.000',
  null,
  null);
end if;
END
$do$;

-- QA from M1 FMS
INSERT INTO empl.employee (email, lastname,firstname,patronymic_name,
  department,position,level,current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) select
  'Jenson.Curtis@stm-labs.ru',
  'Curtis', 'Jenson',null,
  (select id from dict.department  where name='Development' limit 1),
  (select id from dict.position  where name='Automation QA' limit 1),
  (select id from dict.level  where name='Junior' limit 1),
  (select id from proj.project  where name='M1 FMS' limit 1),
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'male',
  '2011-10-01 00:00:00.000',
  null,
  null where NOT EXISTS (SELECT id from empl.employee where email ilike 'Jenson.Curtis@stm-labs.ru');

-- Lead of M1 Fms
DO
$do$
BEGIN
IF NOT EXISTS (SELECT id from empl.employee where email ilike 'Jawad.Mcghee@stm-labs.ru') then
INSERT INTO empl.employee (email, lastname,firstname,patronymic_name,
  department,position,level,current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) VALUES
(
  'Jawad.Mcghee@stm-labs.ru',
  'Mcghee', 'Jawad',null,
  (select id from dict.department  where name='Development' limit 1),
  (select id from dict.position  where name='Project Manager' limit 1),
  (select id from dict.level  where name='Senior' limit 1),
  null,
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'male',
  '2011-10-01 00:00:00.000',
  null,
  null);
INSERT INTO sec.employee_accessible_projects (employee_id, project_id) values (
(select id from empl.employee  where email ilike 'Jawad.Mcghee@stm-labs.ru' limit 1),
(select id from proj.project  where name='M1 FMS' limit 1)
);

INSERT INTO sec.user_role (employee_id, role) values (
(select id from empl.employee  where email ilike 'Jawad.Mcghee@stm-labs.ru' limit 1),
'pm'
);
end if;
END
$do$;

-- Java Developer from M1 Policy Manager
INSERT INTO empl.employee (email, lastname,firstname,patronymic_name,
  department,position,level,current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) select
  'Amy.Beck@stm-labs.ru',
  'Beck', 'Amy',null,
  (select id from dict.department  where name='Development' limit 1),
  (select id from dict.position  where name='Java Developer' limit 1),
  (select id from dict.level  where name='Junior' limit 1),
  (select id from proj.project  where name='M1 Policy Manager' limit 1),
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'female',
  '2011-10-01 00:00:00.000',
  null,
  null where NOT EXISTS (SELECT id from empl.employee where email ilike 'Amy.Beck@stm-labs.ru');

-- Lead of projects pack
DO
$do$
BEGIN
IF NOT EXISTS (SELECT id from empl.employee where email ilike 'Kyran.Neville@stm-labs.ru') then
INSERT INTO empl.employee (email, lastname,firstname,patronymic_name,
  department,position,level,current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) VALUES
(
  'Kyran.Neville@stm-labs.ru',
  'Neville', 'Kyran',null,
  (select id from dict.department  where name='Development' limit 1),
  (select id from dict.position  where name='Java Developer' limit 1),
  (select id from dict.level  where name='Senior' limit 1),
  null,
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'male',
  '2011-10-01 00:00:00.000',
  null,
  null);
INSERT INTO sec.employee_accessible_projects

 (employee_id, project_id) values (
(select id from empl.employee  where email ilike 'Kyran.Neville@stm-labs.ru' limit 1),
(select id from proj.project  where name='M1 Billing' limit 1)
);
INSERT INTO sec.employee_accessible_projects

 (employee_id, project_id) values (
(select id from empl.employee  where email ilike 'Kyran.Neville@stm-labs.ru' limit 1),
(select id from proj.project  where name='M1 FMS' limit 1)
);
INSERT INTO sec.employee_accessible_projects

 (employee_id, project_id) values (
(select id from empl.employee  where email ilike 'Kyran.Neville@stm-labs.ru' limit 1),
(select id from proj.project  where name='M1 Policy Manager' limit 1)
);

INSERT INTO sec.user_role (employee_id, role) values (
(select id from empl.employee  where email ilike 'Kyran.Neville@stm-labs.ru' limit 1),
'pm'
);

end if;
END
$do$;

-- QA from M1 ERP Integration
INSERT INTO empl.employee (email, lastname,firstname,patronymic_name,
  department,position,level,current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) select
  'Jonas.Martin@stm-labs.ru',
  'Martin', 'Jonas',null,
  (select id from dict.department  where name='Integration' limit 1),
  (select id from dict.position  where name='Automation QA' limit 1),
  (select id from dict.level  where name='Senior' limit 1),
  (select id from proj.project  where name='M1 ERP Integration' limit 1),
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'male',
  '2011-10-01 00:00:00.000',
  null,
  null where NOT EXISTS (SELECT id from empl.employee where email ilike 'Jonas.Martin@stm-labs.ru');

-- Technical Lead of the whole company
DO
$do$
BEGIN
IF NOT EXISTS (SELECT id from empl.employee where email ilike 'Toby.Barrow@stm-labs.ru') then
INSERT INTO empl.employee (email, lastname,firstname,patronymic_name,
  department,position,level,current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) VALUES
(
  'Toby.Barrow@stm-labs.ru',
  'Barrow', 'Toby',null,
  (select id from dict.department  where name='Development' limit 1),
  (select id from dict.position  where name='Java Developer' limit 1),
  (select id from dict.level  where name='Senior' limit 1),
  (select id from proj.project  where name='M1 Billing' limit 1),
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'male',
  '2011-10-01 00:00:00.000',
  null,
  null);
INSERT INTO sec.employee_accessible_departments

 (employee_id, department_id) values (
(select id from empl.employee  where email ilike 'Toby.Barrow@stm-labs.ru' limit 1),
(select id from dict.department  where name='Development' limit 1)
);
INSERT INTO sec.employee_accessible_departments

 (employee_id, department_id) values (
(select id from empl.employee  where email ilike 'Toby.Barrow@stm-labs.ru' limit 1),
(select id from dict.department  where name='Integration' limit 1)
);
INSERT INTO sec.user_role (employee_id, role) values (
(select id from empl.employee  where email ilike 'Toby.Barrow@stm-labs.ru' limit 1),
'pm'
);
end if;
END
$do$;

-- HR
DO
$do$
BEGIN
IF NOT EXISTS (SELECT id from empl.employee where email ilike 'Maysa.Sheppard@stm-labs.ru') then
INSERT INTO empl.employee (email, lastname,firstname,patronymic_name,
  department,position,level,current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) VALUES
(
  'Maysa.Sheppard@stm-labs.ru',
  'Sheppard', 'Maysa',null,
  (select id from dict.department  where name='Development' limit 1),
  (select id from dict.position  where name='Java Developer' limit 1),
  (select id from dict.level  where name='Senior' limit 1),
  (select id from proj.project  where name='M1 Billing' limit 1),
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'female',
  '2011-10-01 00:00:00.000',
  null,
  null);
INSERT INTO sec.employee_accessible_departments
 (employee_id, department_id) values (
(select id from empl.employee  where email ilike 'Maysa.Sheppard@stm-labs.ru' limit 1),
(select id from dict.department  where name='Development' limit 1)
);
INSERT INTO sec.employee_accessible_departments
 (employee_id, department_id) values (
(select id from empl.employee  where email ilike 'Maysa.Sheppard@stm-labs.ru' limit 1),
(select id from dict.department  where name='Integration' limit 1)
);
INSERT INTO sec.user_role (employee_id, role) values (
(select id from empl.employee  where email ilike 'Maysa.Sheppard@stm-labs.ru' limit 1),
'hr'
);

end if;
END
$do$;

-- Global Admin
DO
$do$
BEGIN
IF NOT EXISTS (SELECT id from empl.employee where email ilike 'Shaan.Pitts@stm-labs.ru') then
INSERT INTO empl.employee (email, lastname,firstname,patronymic_name,
  department,position,level,current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) VALUES
(
  'Shaan.Pitts@stm-labs.ru',
  'Pitts', 'Shaan',null,
  (select id from dict.department where name='Development' limit 1),
  (select id from dict.position  where name='Java Developer' limit 1),
  (select id from dict.level  where name='Senior' limit 1),
  (select id from proj.project  where name='M1 Billing' limit 1),
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'female',
  '2011-10-01 00:00:00.000',
  null,
  null);
INSERT INTO sec.employee_accessible_departments
 (employee_id, department_id) values (
(select id from empl.employee  where email ilike 'Shaan.Pitts@stm-labs.ru' limit 1),
(select id from dict.department  where name='Development' limit 1)
);
INSERT INTO sec.employee_accessible_departments
 (employee_id, department_id) values (
(select id from empl.employee  where email ilike 'Shaan.Pitts@stm-labs.ru' limit 1),
(select id from dict.department  where name='Integration' limit 1)
);
INSERT INTO sec.user_role (employee_id, role) values (
(select id from empl.employee  where email ilike 'Shaan.Pitts@stm-labs.ru' limit 1),
'global_admin'
);
end if;
END
$do$;
