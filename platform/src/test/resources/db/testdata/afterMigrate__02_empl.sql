-- Safe scripts with empl test data

-- Java Developer from M1 Billing
IF NOT EXISTS (SELECT id from employee where email='Haiden.Spooner@stm-labs.ru')
begin
INSERT INTO employee (email, lastname,firstname,patronymic_name,
  department,[position],[level],current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) VALUES
(
  'Haiden.Spooner@stm-labs.ru',
  'Spooner', 'Haiden',null,
  (select top 1 id from department where name='Development'),
  (select top 1 id from dict_position where name='Java Developer'),
  (SELECT top 1 id from dict_level where name='Senior'),
  (SELECT top 1 id from project where name='M1 Billing'),
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'male',
  '2011-10-01 00:00:00.000',
  null,
  null)
INSERT INTO sec_user (email, employee_id) values (
    'Haiden.Spooner@stm-labs.ru',
    (SELECT top 1 id from employee where email='Haiden.Spooner@stm-labs.ru')
)
end

-- Java Developer from M1 Billing
IF NOT EXISTS (SELECT id from employee where email='Asiyah.Bob@stm-labs.ru')
begin
INSERT INTO employee (email, lastname,firstname,patronymic_name,
  department,[position],[level],current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) VALUES
(
  'Asiyah.Bob@stm-labs.ru',
  'Bob', 'Asiyah',null,
  (select top 1 id from department where name='Development'),
  (select top 1 id from dict_position where name='Java Developer'),
  (SELECT top 1 id from dict_level where name='Senior'),
  (SELECT top 1 id from project where name='M1 Billing'),
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'male',
  '2011-10-01 00:00:00.000',
  null,
  null)
INSERT INTO sec_user (email, employee_id) values (
    'Asiyah.Bob@stm-labs.ru',
    (SELECT top 1 id from employee where email='Asiyah.Bob@stm-labs.ru')
)
end

-- Fired Java Developer from M1 Billing
IF NOT EXISTS (SELECT id from employee where email='Dev.Fired@stm-labs.ru')
begin
INSERT INTO employee (email, lastname,firstname,patronymic_name,
  department,[position],[level],current_project, phone, birthday,sex,date_of_employment,
  office_location,ext_erp_id, date_of_dismissal) VALUES
(
  'Dev.Fired@stm-labs.ru',
  'Fired', 'Dev',null,
  (select top 1 id from department where name='Development'),
  (select top 1 id from dict_position where name='Java Developer'),
  (SELECT top 1 id from dict_level where name='Senior'),
  (SELECT top 1 id from project where name='M1 Billing'),
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'male',
  '2011-10-01 00:00:00.000',
  null,
  null,
  '2000-06-12 00:00:00.000')
INSERT INTO sec_user (email, employee_id) values (
    'Dev.Fired@stm-labs.ru',
    (SELECT top 1 id from employee where email='Dev.Fired@stm-labs.ru')
)
end

-- M1 Billing Project Lead
IF NOT EXISTS (SELECT id from employee where email='Maxwell.May@stm-labs.ru')
begin
INSERT INTO employee (email, lastname,firstname,patronymic_name,
  department,[position],[level],current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) VALUES
(
  'Maxwell.May@stm-labs.ru',
  'May', 'Maxwell',null,
  (select top 1 id from department where name='Development'),
  (select top 1 id from dict_position where name='Project Manager'),
  (SELECT top 1 id from dict_level where name='Senior'),
  (SELECT top 1 id from project where name='M1 Billing'),
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'male',
  '2011-10-01 00:00:00.000',
  null,
  null)
INSERT INTO sec_user (email, employee_id) values (
    'Maxwell.May@stm-labs.ru',
    (SELECT top 1 id from employee where email='Maxwell.May@stm-labs.ru')
)
INSERT INTO employee_accessible_projects (employee_id, project_id) values (
(SELECT top 1 id from employee where email='Maxwell.May@stm-labs.ru'),
(SELECT top 1 id from project where name='M1 Billing')
)

INSERT INTO sec_user_role (user_id, role) values (
(SELECT top 1 id from sec_user where email='Maxwell.May@stm-labs.ru'),
'pm'
)

end

-- Development Department Lead
IF NOT EXISTS (SELECT id from employee where email='Percy.Gough@stm-labs.ru')
begin
INSERT INTO employee (email, lastname,firstname,patronymic_name,
  department,[position],[level],current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) VALUES
(
  'Percy.Gough@stm-labs.ru',
  'Gough', 'Percy',null,
  (select top 1 id from department where name='Development'),
  (select top 1 id from dict_position where name='Head of Department'),
  (SELECT top 1 id from dict_level where name='Senior'),
  null,
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'male',
  '2011-10-01 00:00:00.000',
  null,
  null)
INSERT INTO sec_user (email, employee_id) values (
    'Percy.Gough@stm-labs.ru',
    (SELECT top 1 id from employee where email='Percy.Gough@stm-labs.ru')
)
INSERT INTO employee_accessible_departments (employee_id, department_id) values (
(SELECT top 1 id from employee where email='Percy.Gough@stm-labs.ru'),
(SELECT top 1 id from department where name='Development')
)
INSERT INTO sec_user_role (user_id, role) values (
(SELECT top 1 id from sec_user where email='Percy.Gough@stm-labs.ru'),
'pm'
)
end


-- Java Developer from M1 FMS
IF NOT EXISTS (SELECT id from employee where email='Ammara.Knott@stm-labs.ru')
begin
INSERT INTO employee (email, lastname,firstname,patronymic_name,
  department,[position],[level],current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) VALUES
(
  'Ammara.Knott@stm-labs.ru',
  'Knott', 'Ammara',null,
  (select top 1 id from department where name='Development'),
  (select top 1 id from dict_position where name='Java Developer'),
  (SELECT top 1 id from dict_level where name='Senior'),
  (SELECT top 1 id from project where name='M1 FMS'),
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'female',
  '2011-10-01 00:00:00.000',
  null,
  null)
INSERT INTO sec_user (email, employee_id) values (
    'Ammara.Knott@stm-labs.ru',
    (SELECT top 1 id from employee where email='Ammara.Knott@stm-labs.ru')
)
end

-- QA from M1 FMS
IF NOT EXISTS (SELECT id from employee where email='Jenson.Curtis@stm-labs.ru')
begin
INSERT INTO employee (email, lastname,firstname,patronymic_name,
  department,[position],[level],current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) VALUES
(
  'Jenson.Curtis@stm-labs.ru',
  'Curtis', 'Jenson',null,
  (select top 1 id from department where name='Development'),
  (select top 1 id from dict_position where name='Automation QA'),
  (SELECT top 1 id from dict_level where name='Junior'),
  (SELECT top 1 id from project where name='M1 FMS'),
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'male',
  '2011-10-01 00:00:00.000',
  null,
  null)
INSERT INTO sec_user (email, employee_id) values (
    'Jenson.Curtis@stm-labs.ru',
    (SELECT top 1 id from employee where email='Jenson.Curtis@stm-labs.ru')
)
end

-- Lead of M1 Fms
IF NOT EXISTS (SELECT id from employee where email='Jawad.Mcghee@stm-labs.ru')
begin
INSERT INTO employee (email, lastname,firstname,patronymic_name,
  department,[position],[level],current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) VALUES
(
  'Jawad.Mcghee@stm-labs.ru',
  'Mcghee', 'Jawad',null,
  (select top 1 id from department where name='Development'),
  (select top 1 id from dict_position where name='Project Manager'),
  (SELECT top 1 id from dict_level where name='Senior'),
  null,
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'male',
  '2011-10-01 00:00:00.000',
  null,
  null)
INSERT INTO sec_user (email, employee_id) values (
    'Jawad.Mcghee@stm-labs.ru',
    (SELECT top 1 id from employee where email='Jawad.Mcghee@stm-labs.ru')
)
INSERT INTO employee_accessible_projects (employee_id, project_id) values (
(SELECT top 1 id from employee where email='Jawad.Mcghee@stm-labs.ru'),
(SELECT top 1 id from project where name='M1 FMS')
)

INSERT INTO sec_user_role (user_id, role) values (
(SELECT top 1 id from sec_user where email='Jawad.Mcghee@stm-labs.ru'),
'pm'
)

end

-- Java Developer from M1 Policy Manager
IF NOT EXISTS (SELECT id from employee where email='Amy.Beck@stm-labs.ru')
begin
INSERT INTO employee (email, lastname,firstname,patronymic_name,
  department,[position],[level],current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) VALUES
(
  'Amy.Beck@stm-labs.ru',
  'Beck', 'Amy',null,
  (select top 1 id from department where name='Development'),
  (select top 1 id from dict_position where name='Java Developer'),
  (SELECT top 1 id from dict_level where name='Junior'),
  (SELECT top 1 id from project where name='M1 Policy Manager'),
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'female',
  '2011-10-01 00:00:00.000',
  null,
  null)
INSERT INTO sec_user (email, employee_id) values (
    'Amy.Beck@stm-labs.ru',
    (SELECT top 1 id from employee where email='Amy.Beck@stm-labs.ru')
)
end

-- Lead of projects pack
IF NOT EXISTS (SELECT id from employee where email='Kyran.Neville@stm-labs.ru')
begin
INSERT INTO employee (email, lastname,firstname,patronymic_name,
  department,[position],[level],current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) VALUES
(
  'Kyran.Neville@stm-labs.ru',
  'Neville', 'Kyran',null,
  (select top 1 id from department where name='Development'),
  (select top 1 id from dict_position where name='Java Developer'),
  (SELECT top 1 id from dict_level where name='Senior'),
  null,
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'male',
  '2011-10-01 00:00:00.000',
  null,
  null)
INSERT INTO sec_user (email, employee_id) values (
    'Kyran.Neville@stm-labs.ru',
    (SELECT top 1 id from employee where email='Kyran.Neville@stm-labs.ru')
)
INSERT INTO employee_accessible_projects (employee_id, project_id) values (
(SELECT top 1 id from employee where email='Kyran.Neville@stm-labs.ru'),
(SELECT top 1 id from project where name='M1 Billing')
)
INSERT INTO employee_accessible_projects (employee_id, project_id) values (
(SELECT top 1 id from employee where email='Kyran.Neville@stm-labs.ru'),
(SELECT top 1 id from project where name='M1 FMS')
)
INSERT INTO employee_accessible_projects (employee_id, project_id) values (
(SELECT top 1 id from employee where email='Kyran.Neville@stm-labs.ru'),
(SELECT top 1 id from project where name='M1 Policy Manager')
)

INSERT INTO sec_user_role (user_id, role) values (
(SELECT top 1 id from sec_user where email='Kyran.Neville@stm-labs.ru'),
'pm'
)

end

-- QA from M1 ERP Integration
IF NOT EXISTS (SELECT id from employee where email='Jonas.Martin@stm-labs.ru')
begin
INSERT INTO employee (email, lastname,firstname,patronymic_name,
  department,[position],[level],current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) VALUES
(
  'Jonas.Martin@stm-labs.ru',
  'Martin', 'Jonas',null,
  (select top 1 id from department where name='Integration'),
  (select top 1 id from dict_position where name='Automation QA'),
  (SELECT top 1 id from dict_level where name='Senior'),
  (SELECT top 1 id from project where name='M1 ERP Integration'),
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'male',
  '2011-10-01 00:00:00.000',
  null,
  null)
INSERT INTO sec_user (email, employee_id) values (
    'Jonas.Martin@stm-labs.ru',
    (SELECT top 1 id from employee where email='Jonas.Martin@stm-labs.ru')
)
end

-- Technical Lead of the whole company
IF NOT EXISTS (SELECT id from employee where email='Toby.Barrow@stm-labs.ru')
begin
INSERT INTO employee (email, lastname,firstname,patronymic_name,
  department,[position],[level],current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) VALUES
(
  'Toby.Barrow@stm-labs.ru',
  'Barrow', 'Toby',null,
  (select top 1 id from department where name='Development'),
  (select top 1 id from dict_position where name='Java Developer'),
  (SELECT top 1 id from dict_level where name='Senior'),
  (SELECT top 1 id from project where name='M1 Billing'),
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'male',
  '2011-10-01 00:00:00.000',
  null,
  null)
INSERT INTO sec_user (email, employee_id) values (
    'Toby.Barrow@stm-labs.ru',
    (SELECT top 1 id from employee where email='Toby.Barrow@stm-labs.ru')
)
INSERT INTO employee_accessible_departments (employee_id, department_id) values (
(SELECT top 1 id from employee where email='Toby.Barrow@stm-labs.ru'),
(SELECT top 1 id from department where name='Development')
)
INSERT INTO employee_accessible_departments (employee_id, department_id) values (
(SELECT top 1 id from employee where email='Toby.Barrow@stm-labs.ru'),
(SELECT top 1 id from department where name='Integration')
)
INSERT INTO sec_user_role (user_id, role) values (
(SELECT top 1 id from sec_user where email='Toby.Barrow@stm-labs.ru'),
'pm'
)
end

-- HR
IF NOT EXISTS (SELECT id from employee where email='Maysa.Sheppard@stm-labs.ru')
begin
INSERT INTO employee (email, lastname,firstname,patronymic_name,
  department,[position],[level],current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) VALUES
(
  'Maysa.Sheppard@stm-labs.ru',
  'Sheppard', 'Maysa',null,
  (select top 1 id from department where name='Development'),
  (select top 1 id from dict_position where name='Java Developer'),
  (SELECT top 1 id from dict_level where name='Senior'),
  (SELECT top 1 id from project where name='M1 Billing'),
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'female',
  '2011-10-01 00:00:00.000',
  null,
  null)
INSERT INTO sec_user (email, employee_id) values (
    'Maysa.Sheppard@stm-labs.ru',
    (SELECT top 1 id from employee where email='Maysa.Sheppard@stm-labs.ru')
)
INSERT INTO employee_accessible_departments (employee_id, department_id) values (
(SELECT top 1 id from employee where email='Maysa.Sheppard@stm-labs.ru'),
(SELECT top 1 id from department where name='Development')
)
INSERT INTO employee_accessible_departments (employee_id, department_id) values (
(SELECT top 1 id from employee where email='Maysa.Sheppard@stm-labs.ru'),
(SELECT top 1 id from department where name='Integration')
)
INSERT INTO sec_user_role (user_id, role) values (
(SELECT top 1 id from sec_user where email='Maysa.Sheppard@stm-labs.ru'),
'hr'
)

end

-- Global Admin
IF NOT EXISTS (SELECT id from employee where email='Shaan.Pitts@stm-labs.ru')
begin
INSERT INTO employee (email, lastname,firstname,patronymic_name,
  department,[position],[level],current_project, phone, birthday,sex,date_of_employment, office_location,ext_erp_id) VALUES
(
  'Shaan.Pitts@stm-labs.ru',
  'Pitts', 'Shaan',null,
  (select top 1 id from department where name='Development'),
  (select top 1 id from dict_position where name='Java Developer'),
  (SELECT top 1 id from dict_level where name='Senior'),
  (SELECT top 1 id from project where name='M1 Billing'),
  '+79998884455',
  '1990-06-12 00:00:00.000',
  'female',
  '2011-10-01 00:00:00.000',
  null,
  null)
INSERT INTO sec_user (email, employee_id) values (
    'Shaan.Pitts@stm-labs.ru',
    (SELECT top 1 id from employee where email='Shaan.Pitts@stm-labs.ru')
)
INSERT INTO employee_accessible_departments (employee_id, department_id) values (
(SELECT top 1 id from employee where email='Shaan.Pitts@stm-labs.ru'),
(SELECT top 1 id from department where name='Development')
)
INSERT INTO employee_accessible_departments (employee_id, department_id) values (
(SELECT top 1 id from employee where email='Shaan.Pitts@stm-labs.ru'),
(SELECT top 1 id from department where name='Integration')
)
INSERT INTO sec_user_role (user_id, role) values (
(SELECT top 1 id from sec_user where email='Shaan.Pitts@stm-labs.ru'),
'global_admin'
)

end

