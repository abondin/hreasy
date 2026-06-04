-- Join lastname, firstname and patronymic_name to display name
ALTER TABLE empl.employee ADD COLUMN IF NOT EXISTS
    display_name varchar(256) NULL;
COMMENT ON COLUMN empl.employee.display_name IS 'Employee name in "lastname firstname patronymic name" format';

UPDATE empl.employee set display_name = trim(concat_ws(' ', lastname, firstname, patronymic_name));
UPDATE empl.employee set display_name = 'UNKNOWN' where display_name = null;


ALTER TABLE empl.employee_history ADD COLUMN IF NOT EXISTS
    display_name varchar(256) NULL;
COMMENT ON COLUMN empl.employee_history.display_name IS 'Employee name in "lastname firstname patronymic name" format';

UPDATE empl.employee_history set display_name = trim(concat_ws(' ', lastname, firstname, patronymic_name));
UPDATE empl.employee_history set display_name = 'UNKNOWN' where display_name = null;

alter table empl.employee alter column display_name set not null;


drop view empl.v_employee_detailed;
-- empl.v_employee_detailed source

CREATE OR REPLACE VIEW empl.v_employee_detailed
AS SELECT e.id,
    e.email,
    e.display_name,
    e.birthday,
    e.sex,
    e.date_of_employment,
    e.department,
    e."position",
    e.level,
    e.work_type,
    e.work_day,
    e.phone,
    e.skype,
    e.registration_address,
    e.document_series,
    e.document_number,
    e.document_issued_by,
    e.document_issued_date,
    e.foreign_passport,
    e.city_of_residence,
    e.english_level,
    e.family_status,
    e.spouse_name,
    e.children,
    e.date_of_dismissal,
    e.current_project,
    e.office_location,
    e.ext_erp_id,
    e.telegram,
    e.current_project_role,
    d.name AS department_name,
    l.name AS level_name,
    p.name AS position_name,
    p.category AS position_category,
    project.name AS current_project_name,
    o.id AS office_location_id,
    o.name AS office_location_name,
    sk.rating AS aggregated_skills,
    ba.id AS ba_id,
    ba.name AS ba_name
   FROM empl.employee e
     LEFT JOIN dict.department d ON e.department = d.id
     LEFT JOIN dict.level l ON e.level = l.id
     LEFT JOIN dict."position" p ON e."position" = p.id
     LEFT JOIN proj.project project ON e.current_project = project.id
     LEFT JOIN ba.business_account ba ON project.ba_id = ba.id
     LEFT JOIN dict.office_location o ON e.office_location = o.id
     LEFT JOIN ( SELECT sr.employee_id,
            string_agg(concat_ws('|#|'::text, sr.id, sr.name, gr.id, gr.name, sr.str), '|$|'::text) AS rating
           FROM ( SELECT s.employee_id,
                    s.group_id,
                    s.id,
                    s.name,
                    string_agg(concat_ws(','::text, r.created_by, r.rating), '/'::text) AS str
                   FROM empl.skill s
                     LEFT JOIN empl.skill_rating r ON s.id = r.skill_id AND r.deleted_at IS NULL
                  WHERE s.deleted_at IS NULL
                  GROUP BY s.employee_id, s.group_id, s.id, s.name) sr
             JOIN empl.skill_group gr ON gr.id = sr.group_id
          GROUP BY sr.employee_id) sk ON e.id = sk.employee_id;


ALTER TABLE empl.employee DROP COLUMN IF EXISTS lastname;
ALTER TABLE empl.employee DROP COLUMN IF EXISTS firstname;
ALTER TABLE empl.employee DROP COLUMN IF EXISTS patronymic_name;

ALTER TABLE empl.employee_history DROP COLUMN IF EXISTS lastname;
ALTER TABLE empl.employee_history DROP COLUMN IF EXISTS firstname;
ALTER TABLE empl.employee_history DROP COLUMN IF EXISTS patronymic_name;
