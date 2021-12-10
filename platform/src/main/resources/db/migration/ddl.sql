
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

