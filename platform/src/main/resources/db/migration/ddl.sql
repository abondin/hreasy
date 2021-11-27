
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

