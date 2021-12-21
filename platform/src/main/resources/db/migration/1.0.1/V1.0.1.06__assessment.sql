CREATE SCHEMA IF NOT EXISTS assmnt;

---------- Assessment
CREATE SEQUENCE IF NOT EXISTS assmnt.ASSESSMENT_ID_SEQ;
CREATE TABLE IF NOT EXISTS assmnt.assessment (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('assmnt.ASSESSMENT_ID_SEQ'),
	employee integer NOT NULL REFERENCES empl.employee (id),
	planned_date date NOT NULL,
	created_at timestamp with time zone NULL,
	created_by integer NULL REFERENCES empl.employee (id),
	completed_at timestamp with time zone NULL,
	completed_by integer NULL REFERENCES empl.employee (id),
	canceled_at timestamp with time zone NULL,
	canceled_by integer NULL REFERENCES empl.employee (id)
);
COMMENT ON TABLE assmnt.assessment IS 'Employee Assessment';
COMMENT ON COLUMN assmnt.assessment.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN assmnt.assessment.employee IS 'Link to employee to assessment';
COMMENT ON COLUMN assmnt.assessment.created_at IS 'Created at';
COMMENT ON COLUMN assmnt.assessment.created_by IS 'Created by (link to employee)';
COMMENT ON COLUMN assmnt.assessment.completed_at IS 'Completed at';
COMMENT ON COLUMN assmnt.assessment.completed_by IS 'Completed by (link to employee)';
COMMENT ON COLUMN assmnt.assessment.canceled_at IS 'Canceled at';
COMMENT ON COLUMN assmnt.assessment.canceled_by IS 'Canceled by (link to employee)';

----------- Assessment Form
CREATE SEQUENCE IF NOT EXISTS assmnt.ASSESSMENT_FORM_ID_SEQ;
CREATE TABLE IF NOT EXISTS assmnt.assessment_form (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('assmnt.ASSESSMENT_FORM_ID_SEQ'),
	assessment_id integer NOT NULL REFERENCES assmnt.assessment (id),
	"owner" integer NOT NULL REFERENCES empl.employee (id),
	form_type integer NOT NULL,
	"content" jsonb NULL,
	completed_at timestamp with time zone NULL,
	completed_by integer NULL REFERENCES empl.employee (id)
);
COMMENT ON TABLE assmnt.assessment_form IS 'Employee Assessment Form. Every participant of assessment process fill his own form';
COMMENT ON COLUMN assmnt.assessment_form.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN assmnt.assessment_form.assessment_id IS 'Link to assessment process';
COMMENT ON COLUMN assmnt.assessment_form.owner IS 'Link to the owner (employee who should fill the form)';
COMMENT ON COLUMN assmnt.assessment_form.form_type IS 'Type of the form. Supported values for 2021/11/22: 1 - self assessment, 2 - manager feedback, 3 - meeting notes, 4 - conclusion and decision';
COMMENT ON COLUMN assmnt.assessment_form.content IS 'JSON content of the form. The structure has not been designed yet (2021/11/22)';
COMMENT ON COLUMN assmnt.assessment_form.completed_at IS 'Completed at';
COMMENT ON COLUMN assmnt.assessment_form.completed_by IS 'Completed by (link to employee)';

----- Assessment Form History
CREATE SEQUENCE IF NOT EXISTS assmnt.ASSESSMENT_FORM_HISTORY_ID_SEQ;
CREATE TABLE IF NOT EXISTS assmnt.assessment_form_history (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('assmnt.ASSESSMENT_FORM_HISTORY_ID_SEQ'),
	assessment_form_id integer NOT NULL REFERENCES assmnt.assessment_form (id),
	"content" jsonb NULL,
	created_at timestamp with time zone NULL,
	created_by integer NULL
);
COMMENT ON TABLE assmnt.assessment_form_history IS 'Employee assessment form content history';
COMMENT ON COLUMN assmnt.assessment_form_history.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN assmnt.assessment_form_history.assessment_form_id IS 'Link to assessment form';
COMMENT ON COLUMN assmnt.assessment_form_history.content IS 'JSON content of the form. The structure depends on assmnt.assessment_form_template.content';
COMMENT ON COLUMN assmnt.assessment_form_history.created_at IS 'History record created at';
COMMENT ON COLUMN assmnt.assessment_form_history.created_by IS 'History record created by';


----- Assessment Form Template
CREATE TABLE IF NOT EXISTS assmnt.assessment_form_template (
	form_type integer NOT NULL,
	"content" jsonb NULL
);
COMMENT ON TABLE assmnt.assessment_form_template IS 'Template to fill the assessment form of given type';
COMMENT ON COLUMN assmnt.assessment_form_template.form_type IS 'Form type. See assmnt.assessment_form.form_type';
COMMENT ON COLUMN assmnt.assessment_form_template.content IS 'Structure of the JSON to fill the form';

CREATE SEQUENCE IF NOT EXISTS assmnt.ASSESSMENT_FORM_TEMPLATE_HISTORY_ID_SEQ;
CREATE TABLE IF NOT EXISTS assmnt.assessment_form_template_history (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('assmnt.ASSESSMENT_FORM_TEMPLATE_HISTORY_ID_SEQ'),
	form_type integer NOT NULL,
	"content" jsonb NULL,
	created_at timestamp with time zone NULL,
	created_by integer NULL
);

COMMENT ON TABLE assmnt.assessment_form_template_history IS 'History of Template changes';
COMMENT ON COLUMN assmnt.assessment_form_template_history.form_type IS 'Form type. See assmnt.assessment_form.form_type';
COMMENT ON COLUMN assmnt.assessment_form_template_history.content IS 'Structure of the JSON to fill the form';
COMMENT ON COLUMN assmnt.assessment_form_template_history.created_at IS 'History record created at';
COMMENT ON COLUMN assmnt.assessment_form_template_history.created_by IS 'History record created by';
