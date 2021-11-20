-- Key table of the whole project
CREATE SCHEMA IF NOT EXISTS empl;
CREATE SEQUENCE IF NOT EXISTS empl.EMPLOYEE_ID_SEQ;

CREATE TABLE empl.employee (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('empl.EMPLOYEE_ID_SEQ'),
	email varchar(255) NOT NULL UNIQUE,
	lastname varchar(255) NULL,
	firstname varchar(255) NULL,
	patronymic_name varchar(255) NULL,
	birthday timestamp NULL,
	sex varchar(255) NULL,
	date_of_employment date NULL,
	department integer NULL REFERENCES dict.department (id),
	"position" integer NULL REFERENCES dict.position (id),
	"level" integer NULL REFERENCES dict.level (id),
	work_type varchar(255) NULL,
	work_day varchar(255) NULL,
	phone varchar(255) NULL,
	skype varchar(255) NULL,
	registration_address varchar(255) NULL,
	document_series varchar(255) NULL,
	document_number varchar(255) NULL,
	document_issued_by varchar(255) NULL,
	document_issued_date timestamp NULL,
	foreign_passport varchar(255) NULL,
	city_of_residence varchar(255) NULL,
	english_level varchar(255) NULL,
	family_status varchar(255) NULL,
	spouse_name varchar(255) NULL,
	children varchar(255) NULL,
	date_of_dismissal date NULL,
	current_project integer NULL REFERENCES proj.project (id),
	office_location integer NULL REFERENCES dict.office_location (id),
	ext_erp_id varchar(255) NULL
);

COMMENT ON TABLE empl.employee IS 'Employee. Key entity of the system';
COMMENT ON COLUMN empl.employee.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN empl.employee.email IS 'Email - key attribute of the employee. Uses to login to the system';
COMMENT ON COLUMN empl.employee.lastname IS 'Lastname';
COMMENT ON COLUMN empl.employee.firstname IS 'Firstname';
COMMENT ON COLUMN empl.employee.patronymic_name IS 'Patronymic name';
COMMENT ON COLUMN empl.employee.birthday IS 'Birthday';
COMMENT ON COLUMN empl.employee.sex IS 'Sex';
COMMENT ON COLUMN empl.employee.date_of_employment IS 'Date of Employment';
COMMENT ON COLUMN empl.employee.department IS 'Link to department';
COMMENT ON COLUMN empl.employee."position" IS 'Link to Position dict';
COMMENT ON COLUMN empl.employee."level" IS 'Link to Level dict';
COMMENT ON COLUMN empl.employee.work_type IS 'Work Type. Like Main work or Additional Word';
COMMENT ON COLUMN empl.employee.work_day IS 'Work Day. Like Full time or partial';
COMMENT ON COLUMN empl.employee.phone IS 'Phone in international format (+79998885566)';
COMMENT ON COLUMN empl.employee.skype IS 'Skype account';
COMMENT ON COLUMN empl.employee.registration_address IS 'Address at the place of registration';
COMMENT ON COLUMN empl.employee.document_series IS 'Document Series (part of main document attributes)';
COMMENT ON COLUMN empl.employee.document_number IS 'Document Number (part of main document attributes)';
COMMENT ON COLUMN empl.employee.document_issued_by IS 'Document Issued By (part of main document attributes)';
COMMENT ON COLUMN empl.employee.document_issued_date IS 'Document Issued Date (part of main document attributes)';
COMMENT ON COLUMN empl.employee.foreign_passport IS 'Foreign Passport';
COMMENT ON COLUMN empl.employee.city_of_residence IS 'City of Residence';
COMMENT ON COLUMN empl.employee.english_level IS 'English Level (like Upper - intermediate)';
COMMENT ON COLUMN empl.employee.family_status IS 'Family Status';
COMMENT ON COLUMN empl.employee.spouse_name IS 'Spouse/Husband name';
COMMENT ON COLUMN empl.employee.children IS 'Children';
COMMENT ON COLUMN empl.employee.date_of_dismissal IS 'Date of Dismissal (good luck)';
COMMENT ON COLUMN empl.employee.current_project IS 'Link to the current assigned project';
COMMENT ON COLUMN empl.employee.office_location IS 'Link to office location';
COMMENT ON COLUMN empl.employee.ext_erp_id  IS 'Link to identity of the employee in external ERP system';


----------------- History
CREATE SEQUENCE IF NOT EXISTS empl.EMPLOYEE_HISTORY_ID_SEQ;
CREATE TABLE IF NOT EXISTS empl.employee_history (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('empl.EMPLOYEE_HISTORY_ID_SEQ'),
	employee integer NOT NULL,
	email varchar(255) NULL,
    lastname varchar(255) NULL,
	firstname varchar(255) NULL,
	patronymic_name varchar(255) NULL,
	birthday timestamp NULL,
	sex varchar(255) NULL,
	date_of_employment date NULL,
	department integer NULL,
	"position" integer NULL,
	"level" integer NULL,
	work_type varchar(255) NULL,
	work_day varchar(255) NULL,
	phone varchar(255) NULL,
	skype varchar(255) NULL,
	registration_address varchar(255) NULL,
	document_series varchar(255) NULL,
	document_number varchar(255) NULL,
	document_issued_by varchar(255) NULL,
	document_issued_date timestamp NULL,
	foreign_passport varchar(255) NULL,
	city_of_residence varchar(255) NULL,
	english_level varchar(255) NULL,
	family_status varchar(255) NULL,
	spouse_name varchar(255) NULL,
	children varchar(255) NULL,
	date_of_dismissal date NULL,
	current_project integer NULL,
	office_location integer NULL,
	ext_erp_id varchar(255) NULL,
	created_at timestamp with time zone NOT NULL,
	created_by integer NOT NULL
);

COMMENT ON TABLE empl.employee_history IS 'Employee History Table. Log all changes';
COMMENT ON COLUMN empl.employee_history.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN empl.employee_history.employee IS 'Link to employee';
COMMENT ON COLUMN empl.employee_history.email IS 'Email - key attribute of the employee_history. Uses to login to the system';
COMMENT ON COLUMN empl.employee_history.lastname IS 'Lastname';
COMMENT ON COLUMN empl.employee_history.firstname IS 'Firstname';
COMMENT ON COLUMN empl.employee_history.patronymic_name IS 'Patronymic name';
COMMENT ON COLUMN empl.employee_history.birthday IS 'Birthday';
COMMENT ON COLUMN empl.employee_history.sex IS 'Sex';
COMMENT ON COLUMN empl.employee_history.date_of_employment IS 'Date of Employment';
COMMENT ON COLUMN empl.employee_history.department IS 'Link to department';
COMMENT ON COLUMN empl.employee_history."position" IS 'Link to Position dict';
COMMENT ON COLUMN empl.employee_history."level" IS 'Link to Level dict';
COMMENT ON COLUMN empl.employee_history.work_type IS 'Work Type. Like Main work or Additional Word';
COMMENT ON COLUMN empl.employee_history.work_day IS 'Work Day. Like Full time or partial';
COMMENT ON COLUMN empl.employee_history.phone IS 'Phone in international format (+79998885566)';
COMMENT ON COLUMN empl.employee_history.skype IS 'Skype account';
COMMENT ON COLUMN empl.employee_history.registration_address IS 'Address at the place of registration';
COMMENT ON COLUMN empl.employee_history.document_series IS 'Document Series (part of main document attributes)';
COMMENT ON COLUMN empl.employee_history.document_number IS 'Document Number (part of main document attributes)';
COMMENT ON COLUMN empl.employee_history.document_issued_by IS 'Document Issued By (part of main document attributes)';
COMMENT ON COLUMN empl.employee_history.document_issued_date IS 'Document Issued Date (part of main document attributes)';
COMMENT ON COLUMN empl.employee_history.foreign_passport IS 'Foreign Passport';
COMMENT ON COLUMN empl.employee_history.city_of_residence IS 'City of Residence';
COMMENT ON COLUMN empl.employee_history.english_level IS 'English Level (like Upper - intermediate)';
COMMENT ON COLUMN empl.employee_history.family_status IS 'Family Status';
COMMENT ON COLUMN empl.employee_history.spouse_name IS 'Spouse/Husband name';
COMMENT ON COLUMN empl.employee_history.children IS 'Children';
COMMENT ON COLUMN empl.employee_history.date_of_dismissal IS 'Date of Dismissal (good luck)';
COMMENT ON COLUMN empl.employee_history.current_project IS 'Link to the current assigned project';
COMMENT ON COLUMN empl.employee_history.office_location IS 'Link to office location';
COMMENT ON COLUMN empl.employee_history.ext_erp_id  IS 'Link to identity of the employee in external ERP system';
COMMENT ON COLUMN empl.employee_history.created_at  IS 'History item created at';
COMMENT ON COLUMN empl.employee_history.created_by  IS 'History item created by (link to employee)';