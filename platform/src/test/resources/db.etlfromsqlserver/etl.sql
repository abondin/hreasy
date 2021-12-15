-- This file helps me to migrate data from SQL Server to PostgreSQL
-- I do it in ELT manner:
-- 1) (EL) I use simple DBeaver export to extract data from SQL Server and load it to PostgreSQL (to DTO schema)
-- 2) (T) I use this custom SQL script to transform data from SQL Server like DTO scheme to new DDL

-- Dictionary
INSERT INTO dict.department
(id, "name")
select id, name from dbo.department;
SELECT setval('dict.dep_id_seq', (SELECT MAX(id) FROM dict.department)+1);

INSERT INTO dict.level
(id, "name")
select id, name from dbo.dict_level;
SELECT setval('dict.level_id_seq', (SELECT MAX(id) FROM dict.level)+1);

INSERT INTO dict.office_location
(id, "name", description, office)
select id, "name", description, office from dbo.office_location;
SELECT setval('dict.office_location_id_seq', (SELECT MAX(id) FROM dict.office_location)+1);

INSERT INTO dict."position"
(id, "name", category)
select id, "name", category from dbo.dict_position;
SELECT setval('dict.position_id_seq', (SELECT MAX(id) FROM dict.position)+1);



-- Employee (without project)
INSERT INTO empl.employee
(id, email, lastname, firstname, patronymic_name, birthday, sex, date_of_employment, department, "position", "level", work_type, work_day, phone, skype, registration_address, document_series, document_number, document_issued_by, document_issued_date, foreign_passport, city_of_residence, english_level, family_status, spouse_name, children, date_of_dismissal, current_project, office_location, ext_erp_id)
select
id::numeric::integer
, (case when email IS NULL then concat('noemail-',id,'@stm-labs.ru') else email end) as email
, lastname, firstname, patronymic_name
, birthday::date
, sex
, date_of_employment::date
, department::integer, "position"::integer, "level"::integer, work_type, work_day, phone, skype, registration_address, document_series, document_number, document_issued_by
, document_issued_date::date
, foreign_passport, city_of_residence, english_level, family_status, spouse_name, children
, date_of_dismissal::date
, null, office_location::integer, ext_erp_id
from dbo.employee;
SELECT setval('empl.EMPLOYEE_ID_SEQ', (SELECT MAX(id) FROM empl.employee)+1);


INSERT INTO empl.employee_history
(id, employee, email, lastname, firstname, patronymic_name, birthday, sex, date_of_employment, department, "position", "level", work_type, work_day, phone, skype, registration_address, document_series, document_number, document_issued_by, document_issued_date, foreign_passport, city_of_residence, english_level, family_status, spouse_name, children, date_of_dismissal, current_project, office_location, ext_erp_id, created_at, created_by)
select id, employee, email, lastname, firstname, patronymic_name, birthday::date
, sex, date_of_employment::date
, department, "position", "level", work_type, work_day, phone, skype, registration_address, document_series, document_number, document_issued_by
, document_issued_date::date, foreign_passport, city_of_residence, english_level, family_status, spouse_name, children
, date_of_dismissal::date, current_project, office_location, ext_erp_id, created_at::timestamp with time zone, created_by
from dbo.employee_history;
SELECT setval('empl.EMPLOYEE_HISTORY_ID_SEQ', (SELECT MAX(id) FROM empl.employee_history)+1);

INSERT INTO empl.kids
(id, "name", birthday, parent)
select id, "name", birthday::date, parent from dbo.kids;
SELECT setval('empl.employee_kids_id_seq', (SELECT MAX(id) FROM empl.kids)+1);

INSERT INTO empl.skill_group
(id, "name", description, archived)
select id, "name", description, (case when archived = '1' then true else false end)  archived
from dbo.skill_group;
SELECT setval('empl.skill_group_seq', (SELECT MAX(id) FROM empl.skill_group)+1);

INSERT INTO empl.skill
(id, employee_id, group_id, "name", shared, created_at, created_by, deleted_at, deleted_by)
select id, employee_id, group_id, "name"
, (case when shared = '1' then true else false end) shared
, created_at::timestamp with time zone, created_by
, deleted_at::timestamp with time zone, deleted_by
from dbo.skill;
SELECT setval('empl.skill_seq', (SELECT MAX(id) FROM empl.skill)+1);

INSERT INTO empl.skill_rating
(id, skill_id, rating, notes, created_at, created_by, updated_at, deleted_at, deleted_by)
select id, skill_id, rating, notes
, created_at::timestamp with time zone, created_by, updated_at::timestamp with time zone, deleted_at::timestamp with time zone, deleted_by
from dbo.skill_rating;
SELECT setval('empl.skill_rating_seq', (SELECT MAX(id) FROM empl.skill_rating)+1);

-- Business account
insert into ba.business_account
(id,name, responsible_employee,	description, archived, created_at, created_by)
select id,name, responsible_employee,description, (case when archived='0' then false else true end)::boolean as archived
, created_at::timestamp with time zone
, created_by
from dbo.business_account;
SELECT setval('ba.BA_ID_SEQ', (SELECT MAX(id) FROM ba.business_account)+1);
-- Business account history
insert into ba.business_account_history
(id,ba_id, name, responsible_employee,	description, archived, updated_at, updated_by)
select id,ba_id, name, responsible_employee,description, (case when archived='0' then false else true end)::boolean as archived
, updated_at::timestamp with time zone
, updated_by
from dbo.business_account_history;
SELECT setval('ba.ba_history_id_seq', (SELECT MAX(id) FROM ba.business_account_history)+1);

-- ba_assignment - nothing to migrate
-- ba_assignment_history - nothing to migrate
-- ba_position - nothing to migrate
-- ba_position_history - nothing to migrate

-- Project
INSERT INTO proj.project
(id, ba_id, "name", start_date, end_date, customer, person_of_contact, department_id, created_at, created_by)
select p.id, p.ba_id, p."name"
, p.start_date::date, p.end_date::date, p.customer, poc.name person_of_contact, p.department_id
, p.created_at::timestamp with time zone
, created_by
from dbo.project p left join dbo.project_poc poc on p.person_of_contact=poc.id;
SELECT setval('proj.project_id_seq', (SELECT MAX(id) FROM proj.project)+1);

INSERT INTO proj.project_history
(id, project_id, ba_id, "name", start_date, end_date, customer, person_of_contact, department_id, updated_at, updated_by)
select p.history_id, project_id, p.ba_id, p."name"
, p.start_date::date, p.end_date::date, p.customer, poc.name person_of_contact, p.department_id
, p.updated_at::timestamp with time zone
, updated_by
from dbo.project_history p left join dbo.project_poc poc on p.person_of_contact=poc.id;
SELECT setval('proj.project_history_id_seq', (SELECT MAX(id) FROM proj.project_history)+1);

-- Employee (update project)
update empl.employee e set current_project=d.current_project
from dbo.employee d where d.id=e.id;

-- Security
INSERT INTO sec.perm
("permission", description)
select "permission", description from dbo.sec_perm;

INSERT INTO sec.role
("role", description)
select "role", description from dbo.sec_role;

insert into sec.role_perm
(id, "role", "permission")
select id, "role", permission from dbo.sec_role_perm;
SELECT setval('sec.role_perm_id_seq', (SELECT MAX(id) FROM sec.role_perm)+1);

insert into sec.user_role
(employee_id, "role")
select u.employee_id, "role" from dbo.sec_user_role ur left join dbo.sec_user u on ur.user_id=u.id;

INSERT INTO sec.user_role_history
(id, employee_id, roles, accessible_projects, accessible_bas, accessible_departments, created_at, created_by)
select id, employee_id
, string_to_array(roles, ',') roles
, string_to_array(accessible_projects, ',')::integer[] accessible_projects
, '{}' accessible_bas
, string_to_array(accessible_departments, ',')::integer[] accessible_departments
, updated_at::timestamp with time zone, updated_by
from dbo.sec_user_role_history;
SELECT setval('sec.user_role_history_id_seq', (SELECT MAX(id) FROM sec.user_role_history)+1);


INSERT INTO sec.login_history
(id, login, logged_employee_id, error, login_time)
select id, login, logged_employee_id, error, login_time::timestamp with time zone from dbo.sec_login_history;
SELECT setval('sec.login_history_id_seq', (SELECT MAX(id) FROM sec.login_history)+1);

INSERT INTO sec.employee_accessible_departments
(employee_id, department_id)
select employee_id, department_id from dbo.employee_accessible_departments;

INSERT INTO sec.employee_accessible_projects
(employee_id, project_id)
select employee_id, project_id from dbo.employee_accessible_projects;

-- article
INSERT INTO article.article
(id, article_group, "name", description, "content", moderated, archived, created_at, created_by, updated_at, updated_by)
select id, article_group, "name", description, "content"
, (case when moderated='0' then false else true end)::boolean as moderated
, (case when archived='0' then false else true end)::boolean as archived
, created_at::timestamp with time zone, created_by
, updated_at::timestamp with time zone, updated_by from dbo.article;
SELECT setval('article.article_id_seq', (SELECT MAX(id) FROM article.article)+1);

INSERT INTO article.article_history
(id, article_id, article_group, "name", description, "content", moderated, archived, created_at, created_by)
select id, article_id, article_group, "name", description, "content"
, (case when moderated='0' then false else true end)::boolean as moderated
, (case when archived='0' then false else true end)::boolean as archived
, created_at::timestamp with time zone, created_by
 from dbo.article_history;
SELECT setval('article.article_history_id_seq', (SELECT MAX(id) FROM article.article_history)+1);

-- assessment
INSERT INTO assmnt.assessment
(id, employee, planned_date, created_at, created_by, completed_at, completed_by, canceled_at, canceled_by)
select id, employee,
  planned_date::date
, created_at::timestamp with time zone
, created_by
, completed_at::timestamp with time zone
, completed_by
, canceled_at::timestamp with time zone
, canceled_by
from dbo.assessment;
SELECT setval('assmnt.assessment_id_seq', (SELECT MAX(id) FROM assmnt.assessment)+1);

-- assessment_form no data
-- assessment_form_history no data
-- assessment_form_template no data
-- assessment_form_template_history no data

-- overtimes
INSERT INTO ovt.overtime_report
(id, employee, "period")
select id, employee_id, "period" from dbo.overtime_report;
SELECT setval('ovt.overtime_report_id_seq', (SELECT MAX(id) FROM ovt.overtime_report)+1);

INSERT INTO ovt.overtime_item
(id, report_id, "date", project_id, hours, notes, created_at, created_by, deleted_at, deleted_by)
select id
, report_id
, "date"::date
, project_id, hours, notes
, created_at::timestamp with time zone, created_employee_id
, deleted_at::timestamp with time zone, deleted_employee_id
from dbo.overtime_item;
SELECT setval('ovt.overtime_item_id_seq', (SELECT MAX(id) FROM ovt.overtime_item)+1);

INSERT INTO ovt.overtime_closed_period
("period", closed_at, closed_by, "comment")
select "period", closed_at::timestamp with time zone, closed_by, "comment" from dbo.overtime_closed_period;

INSERT INTO ovt.overtime_period_history
(id, "period", "action", updated_at, updated_by, "comment")
select id, "period", "action", updated_at::timestamp with time zone, updated_by, "comment"
from dbo.overtime_period_history;
SELECT setval('ovt.overtime_period_history_id_seq', (SELECT MAX(id) FROM ovt.overtime_period_history)+1);

INSERT INTO ovt.overtime_approval_decision
(id, report_id, approver, decision, decision_time, cancel_decision_time, "comment")
select id, report_id, approver, decision, decision_time::timestamp with time zone, cancel_decision_time::timestamp with time zone, "comment"
from dbo.overtime_approval_decision;
SELECT setval('ovt.overtime_approval_decision_id_seq', (SELECT MAX(id) FROM ovt.overtime_approval_decision)+1);

-- vacations
INSERT INTO vac.vacation
(id, employee, "year", start_date, end_date, notes, planned_start_date, planned_end_date, days_number, stat, documents, created_at, created_by, updated_at, updated_by)
select id, employee, "year"
, start_date::date, end_date::date, notes
, planned_start_date::date, planned_end_date::date
, days_number, stat, documents
, (case when created_at is null then now() else created_at::timestamp with time zone end) created_at
, (case when created_by is null then 5 else created_by end) created_by
, (case when updated_at is null then now() else updated_at::timestamp with time zone end) updated_at
, (case when updated_by is null then 5 else updated_by end) updated_by
from dbo.vacation;
SELECT setval('vac.vacation_id_seq', (SELECT MAX(id) FROM vac.vacation)+1);

INSERT INTO vac.vacation_history
(id, vacation_id, created_at, created_by, employee, "year", start_date, end_date, planned_start_date, planned_end_date, days_number, stat, documents, notes)
select id, vacation_id, created_at::timestamp with time zone end
, created_by, employee, "year"
, start_date::date, end_date::date, planned_start_date::date, planned_end_date::date, days_number, stat, documents, notes
from dbo.vacation_history;
SELECT setval('vac.vacation_history_id_seq', (SELECT MAX(id) FROM vac.vacation_history)+1);
