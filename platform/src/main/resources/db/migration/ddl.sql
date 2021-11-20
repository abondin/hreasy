-- public.article definition

-- Drop table

-- DROP TABLE public.article;

CREATE TABLE public.article (
	id int4 NOT NULL,
	article_group varchar(255) NULL,
	"name" varchar(255) NULL,
	description text NULL,
	"content" text NULL,
	moderated bit(1) NULL,
	archived bit(1) NULL,
	created_at varchar NULL,
	created_by int4 NULL,
	updated_at varchar NULL,
	updated_by int4 NULL
);


-- public.article_history definition

-- Drop table

-- DROP TABLE public.article_history;

CREATE TABLE public.article_history (
	id int4 NOT NULL,
	article_id int4 NOT NULL,
	article_group varchar(255) NULL,
	"name" varchar(255) NULL,
	description text NULL,
	"content" text NULL,
	moderated bit(1) NULL,
	archived bit(1) NULL,
	created_at varchar NULL,
	created_by int4 NULL
);


-- public.assessment definition

-- Drop table

-- DROP TABLE public.assessment;

CREATE TABLE public.assessment (
	id int4 NOT NULL,
	employee int4 NOT NULL,
	planned_date timestamp NOT NULL,
	created_at varchar NOT NULL,
	created_by int4 NOT NULL,
	completed_at varchar NULL,
	completed_by int4 NULL,
	canceled_at varchar NULL,
	canceled_by int4 NULL
);


-- public.assessment_form definition

-- Drop table

-- DROP TABLE public.assessment_form;

CREATE TABLE public.assessment_form (
	id int4 NOT NULL,
	assessment_id int4 NOT NULL,
	"owner" int4 NOT NULL,
	form_type int4 NOT NULL,
	"content" text NULL,
	completed_at varchar NULL,
	completed_by int4 NULL
);


-- public.assessment_form_history definition

-- Drop table

-- DROP TABLE public.assessment_form_history;

CREATE TABLE public.assessment_form_history (
	id int4 NOT NULL,
	assessment_form_id int4 NOT NULL,
	"content" text NULL,
	updated_at varchar NOT NULL,
	updated_by int4 NOT NULL
);


-- public.assessment_form_template definition

-- Drop table

-- DROP TABLE public.assessment_form_template;

CREATE TABLE public.assessment_form_template (
	form_type int4 NOT NULL,
	"content" text NULL
);


-- public.assessment_form_template_history definition

-- Drop table

-- DROP TABLE public.assessment_form_template_history;

CREATE TABLE public.assessment_form_template_history (
	id int4 NOT NULL,
	form_type int4 NOT NULL,
	"content" text NULL,
	updated_at varchar NOT NULL,
	updated_by int4 NOT NULL
);


-- public.ba_assignment definition

-- Drop table

-- DROP TABLE public.ba_assignment;

CREATE TABLE public.ba_assignment (
	id int4 NOT NULL,
	business_account int4 NOT NULL,
	employee int4 NULL,
	project int4 NULL,
	parent_assignment int4 NULL,
	ba_position int4 NULL,
	employment_rate float4 NULL,
	ba_position_rate float4 NULL,
	"comment" text NULL,
	start_date timestamp NULL,
	end_date timestamp NULL,
	planned_start_date timestamp NULL,
	planned_end_date timestamp NULL,
	archived bit(1) NULL,
	closed_at varchar NULL,
	closed_by int4 NULL,
	closed_reason varchar(255) NULL,
	closed_comment text NULL,
	created_at varchar NULL,
	created_by int4 NULL
);


-- public.ba_assignment_history definition

-- Drop table

-- DROP TABLE public.ba_assignment_history;

CREATE TABLE public.ba_assignment_history (
	id int4 NOT NULL,
	ba_assignment_id int4 NOT NULL,
	business_account int4 NOT NULL,
	employee int4 NULL,
	project int4 NULL,
	parent_assignment int4 NULL,
	ba_position int4 NULL,
	employment_rate float4 NULL,
	ba_position_rate float4 NULL,
	"comment" text NULL,
	start_date timestamp NULL,
	end_date timestamp NULL,
	planned_start_date timestamp NULL,
	planned_end_date timestamp NULL,
	archived bit(1) NULL,
	closed_at varchar NULL,
	closed_by int4 NULL,
	closed_reason varchar(255) NULL,
	closed_comment text NULL,
	updated_at varchar NULL,
	updated_by int4 NULL
);


-- public.ba_position definition

-- Drop table

-- DROP TABLE public.ba_position;

CREATE TABLE public.ba_position (
	id int4 NOT NULL,
	business_account int4 NOT NULL,
	"name" varchar(255) NULL,
	rate numeric(19, 4) NULL,
	description text NULL,
	archived bit(1) NULL,
	created_at varchar NULL,
	created_by int4 NULL
);


-- public.ba_position_history definition

-- Drop table

-- DROP TABLE public.ba_position_history;

CREATE TABLE public.ba_position_history (
	id int4 NOT NULL,
	ba_position_id int4 NOT NULL,
	business_account int4 NOT NULL,
	"name" varchar(255) NULL,
	rate numeric(19, 4) NULL,
	description text NULL,
	archived bit(1) NULL,
	updated_at varchar NULL,
	updated_by int4 NULL
);


-- public.dict_vacancy_position definition

-- Drop table

-- DROP TABLE public.dict_vacancy_position;

CREATE TABLE public.dict_vacancy_position (
	id int4 NOT NULL,
	"name" varchar(255) NULL,
	description varchar(255) NULL
);


-- public.dict_vacancy_priority definition

-- Drop table

-- DROP TABLE public.dict_vacancy_priority;

CREATE TABLE public.dict_vacancy_priority (
	id int4 NOT NULL,
	priority int4 NULL,
	"name" varchar(255) NULL
);


-- public.employee_accessible_departments definition

-- Drop table

-- DROP TABLE public.employee_accessible_departments;

CREATE TABLE public.employee_accessible_departments (
	employee_id int4 NOT NULL,
	department_id int4 NOT NULL
);


-- public.employee_accessible_projects definition

-- Drop table

-- DROP TABLE public.employee_accessible_projects;

CREATE TABLE public.employee_accessible_projects (
	employee_id int4 NOT NULL,
	project_id int4 NOT NULL
);

-- public.kids definition

-- Drop table

-- DROP TABLE public.kids;

CREATE TABLE public.kids (
	id int4 NOT NULL,
	"name" varchar(255) NULL,
	birthday timestamp NULL,
	parent int4 NULL
);



-- public.overtime_approval_decision definition

-- Drop table

-- DROP TABLE public.overtime_approval_decision;

CREATE TABLE public.overtime_approval_decision (
	id int4 NOT NULL,
	report_id int4 NOT NULL,
	approver int4 NOT NULL,
	decision varchar(255) NULL,
	decision_time varchar NOT NULL,
	cancel_decision_time varchar NULL,
	"comment" text NULL
);


-- public.overtime_closed_period definition

-- Drop table

-- DROP TABLE public.overtime_closed_period;

CREATE TABLE public.overtime_closed_period (
	"period" int4 NOT NULL,
	closed_by int4 NOT NULL,
	closed_at varchar NOT NULL,
	"comment" text NULL
);


-- public.overtime_item definition

-- Drop table

-- DROP TABLE public.overtime_item;

CREATE TABLE public.overtime_item (
	id int4 NOT NULL,
	report_id int4 NOT NULL,
	"date" date NOT NULL,
	project_id int4 NOT NULL,
	hours numeric(3, 1) NULL,
	notes varchar(1024) NULL,
	created_at varchar NOT NULL,
	created_employee_id int4 NOT NULL,
	deleted_at varchar NULL,
	deleted_employee_id int4 NULL
);


-- public.overtime_period_history definition

-- Drop table

-- DROP TABLE public.overtime_period_history;

CREATE TABLE public.overtime_period_history (
	id int4 NOT NULL,
	"period" int4 NOT NULL,
	"action" int2 NOT NULL,
	updated_by int4 NOT NULL,
	updated_at varchar NOT NULL,
	"comment" text NULL
);


-- public.overtime_report definition

-- Drop table

-- DROP TABLE public.overtime_report;

CREATE TABLE public.overtime_report (
	id int4 NOT NULL,
	employee_id int4 NOT NULL,
	"period" int4 NOT NULL
);


-- public.sec_login_history definition

-- Drop table

-- DROP TABLE public.sec_login_history;

CREATE TABLE public.sec_login_history (
	id int4 NOT NULL,
	login varchar(255) NULL,
	logged_employee_id int4 NULL,
	error varchar(4000) NULL,
	login_time varchar NOT NULL
);


-- public.sec_perm definition

-- Drop table

-- DROP TABLE public.sec_perm;

CREATE TABLE public.sec_perm (
	"permission" varchar(255) NULL,
	description varchar(1024) NULL
);


-- public.sec_role definition

-- Drop table

-- DROP TABLE public.sec_role;

CREATE TABLE public.sec_role (
	"role" varchar(255) NULL,
	description varchar(1024) NULL
);


-- public.sec_role_perm definition

-- Drop table

-- DROP TABLE public.sec_role_perm;

CREATE TABLE public.sec_role_perm (
	id int4 NOT NULL,
	"role" varchar(255) NULL,
	"permission" varchar(255) NULL
);


-- public.sec_user definition

-- Drop table

-- DROP TABLE public.sec_user;

CREATE TABLE public.sec_user (
	id int4 NOT NULL,
	email varchar(255) NULL,
	employee_id int4 NULL
);


-- public.sec_user_role definition

-- Drop table

-- DROP TABLE public.sec_user_role;

CREATE TABLE public.sec_user_role (
	user_id int4 NOT NULL,
	"role" varchar(255) NULL
);


-- public.sec_user_role_history definition

-- Drop table

-- DROP TABLE public.sec_user_role_history;

CREATE TABLE public.sec_user_role_history (
	id int4 NOT NULL,
	employee_id int4 NOT NULL,
	user_id int4 NULL,
	roles varchar(4000) NULL,
	accessible_projects varchar(4000) NULL,
	accessible_departments varchar(4000) NULL,
	start_date timestamp NULL,
	updated_by int4 NOT NULL,
	updated_at varchar NOT NULL
);


-- public.skill definition

-- Drop table

-- DROP TABLE public.skill;

CREATE TABLE public.skill (
	id int4 NOT NULL,
	employee_id int4 NOT NULL,
	group_id int4 NULL,
	"name" varchar(256) NULL,
	shared bit(1) NOT NULL,
	created_by int4 NOT NULL,
	created_at varchar NOT NULL,
	deleted_by int4 NULL,
	deleted_at varchar NULL
);


-- public.skill_group definition

-- Drop table

-- DROP TABLE public.skill_group;

CREATE TABLE public.skill_group (
	id int4 NOT NULL,
	"name" varchar(256) NULL,
	description varchar(4000) NULL,
	archived bit(1) NOT NULL
);


-- public.skill_rating definition

-- Drop table

-- DROP TABLE public.skill_rating;

CREATE TABLE public.skill_rating (
	id int4 NOT NULL,
	skill_id int4 NOT NULL,
	rating float4 NOT NULL,
	notes varchar(4000) NULL,
	created_by int4 NOT NULL,
	created_at varchar NOT NULL,
	updated_at varchar NOT NULL,
	deleted_by int4 NULL,
	deleted_at varchar NULL
);


-- public.vacation definition

-- Drop table

-- DROP TABLE public.vacation;

CREATE TABLE public.vacation (
	id int4 NOT NULL,
	employee int4 NULL,
	"year" int4 NOT NULL,
	start_date timestamp NULL,
	end_date timestamp NULL,
	notes varchar(255) NULL,
	planned_start_date timestamp NULL,
	planned_end_date timestamp NULL,
	days_number int4 NULL,
	stat int4 NOT NULL,
	documents varchar(255) NULL,
	created_at varchar NULL,
	created_by int4 NULL,
	updated_at varchar NULL,
	updated_by int4 NULL
);


-- public.vacation_history definition

-- Drop table

-- DROP TABLE public.vacation_history;

CREATE TABLE public.vacation_history (
	id int4 NOT NULL,
	vacation_id int4 NULL,
	created_at varchar NOT NULL,
	created_by int4 NOT NULL,
	employee int4 NULL,
	"year" int4 NOT NULL,
	start_date timestamp NULL,
	end_date timestamp NULL,
	planned_start_date timestamp NULL,
	planned_end_date timestamp NULL,
	days_number int4 NULL,
	stat int4 NULL,
	documents varchar(255) NULL,
	notes varchar(255) NULL
);

