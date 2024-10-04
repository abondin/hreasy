CREATE SEQUENCE IF NOT EXISTS dict.OFFICE_ID_SEQ;
CREATE TABLE IF NOT EXISTS dict.office (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('dict.OFFICE_ID_SEQ'),
    "name" varchar(255) NOT NULL,
    address varchar(1024) NULL,
    description varchar(1024) NULL,
    archived boolean NOT NULL DEFAULT false,
    created_at timestamp with time zone NOT NULL,
    created_by integer NOT NULL REFERENCES empl.employee (id),
    map_svg text NULL
);
COMMENT ON TABLE dict.office IS 'Company office';
COMMENT ON COLUMN dict.office.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN dict.office."name" IS 'Office name';
COMMENT ON COLUMN dict.office.address IS 'Office address';
COMMENT ON COLUMN dict.office.description IS 'Office description';
COMMENT ON COLUMN dict.office.map_svg IS 'Office map SVG';

ALTER TABLE dict.office_location ADD COLUMN IF NOT EXISTS office_id int null REFERENCES dict.office(id);
COMMENT ON COLUMN dict.office_location.office IS '!!! DEPRECATED COLUMN !!! Use office_id instead';

DROP TABLE IF EXISTS dict.office_location_log;

COMMENT ON COLUMN history.history.entity_type IS '
  [empl_manager] - Entity type,
  [working_days] - Working Days Calendar,
  [timesheet_record] - Timesheet Record
  [support_request_group] - Support Request Group
  [support_request] - Support Request
  [junior_registry] - Juniors registry
  [junior_registry_report] - Juniors registry
  [office] - Office
  [office_location] - Office Location
  ';

-- Permission to admin office location
INSERT INTO sec.perm (permission,description) VALUES
	 ('admin_office','Create/update/delete office') on conflict do nothing;
INSERT INTO sec.role_perm (role,permission) VALUES
	 ('global_admin','admin_office'),
	 ('hr','admin_office') on conflict do nothing;


ALTER TABLE empl.employee ADD COLUMN IF NOT EXISTS office_workplace varchar(64) null;
ALTER TABLE empl.employee_history ADD COLUMN IF NOT EXISTS office_workplace varchar(64) null;
COMMENT ON COLUMN empl.employee.office_workplace IS 'office workplace';
COMMENT ON COLUMN empl.employee_history.office_workplace IS 'office workplace';


-- empl.v_employee_detailed source
DROP VIEW empl.v_employee_detailed;
CREATE OR REPLACE VIEW empl.v_employee_detailed
AS SELECT e.*,
    d.name AS department_name,
    o.name as organization_name,
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
     LEFT JOIN dict.organization org ON e.organization = org.id
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