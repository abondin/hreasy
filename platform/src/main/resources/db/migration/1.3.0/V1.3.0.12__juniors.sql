CREATE schema if not exists udr;
CREATE TABLE IF NOT EXISTS udr.junior_registry (
	junior_id integer PRIMARY KEY NOT NULL REFERENCES empl.employee (id),
	role VARCHAR(1024) NOT NULL,
	mentor_id integer REFERENCES empl.employee (id),
	budgeting_account integer REFERENCES ba.business_account(id),
	created_at timestamp with time zone not null,
	created_by integer NULL REFERENCES empl.employee (id),
    graduated_at timestamp with time zone null,
   	graduated_by integer REFERENCES empl.employee (id),
   	graduated_comment text null
);

CREATE SEQUENCE IF NOT EXISTS udr.JUNIOR_REPORT_ID_SEQ;
CREATE TABLE IF NOT EXISTS udr.junior_report (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('udr.JUNIOR_REPORT_ID_SEQ'),
	junior_id integer NOT NULL REFERENCES empl.employee (id),
    progress integer not null,
    comment text not null,
	created_at timestamp with time zone not null,
	created_by integer NULL REFERENCES empl.employee (id),
	deleted_at timestamp with time zone null,
   	deleted_by integer REFERENCES empl.employee (id)
);
COMMENT ON TABLE udr.junior_registry IS 'Registry for junior employees';
COMMENT ON COLUMN udr.junior_registry.junior_id IS 'Unique identifier for junior employee';
COMMENT ON COLUMN udr.junior_registry.role IS 'Junior employee role like Java Backend';
COMMENT ON COLUMN udr.junior_registry.mentor_id IS 'Mentor ID for the junior employee';
COMMENT ON COLUMN udr.junior_registry.budgeting_account IS 'Budgeting account ID';
COMMENT ON COLUMN udr.junior_registry.created_at IS 'Timestamp of junior employee registration';
COMMENT ON COLUMN udr.junior_registry.created_by IS 'ID of the user who registered the junior employee';
COMMENT ON COLUMN udr.junior_registry.graduated_at IS 'Timestamp of junior employee graduation (optional)';
COMMENT ON COLUMN udr.junior_registry.graduated_by IS 'ID of the user who approved junior employee graduation (optional)';
COMMENT ON COLUMN udr.junior_registry.graduated_comment IS 'Comment when graduated';

COMMENT ON TABLE udr.junior_report IS 'Junior employee reports';

COMMENT ON COLUMN udr.junior_report.id IS 'Unique identifier for junior employee report';
COMMENT ON COLUMN udr.junior_report.junior_id IS 'Foreign key referencing the junior employee ID';
COMMENT ON COLUMN udr.junior_report.progress IS 'Report progress status:
    1 - Degradation,
    2 - No Progress,
    3 - Progress,
    4 - Good Progress';
COMMENT ON COLUMN udr.junior_report.comment IS 'Free-form comment about the junior employee report';
COMMENT ON COLUMN udr.junior_report.created_at IS 'Timestamp of junior employee report creation';
COMMENT ON COLUMN udr.junior_report.created_by IS 'ID of the user who created the junior employee report';
COMMENT ON COLUMN udr.junior_report.deleted_at IS 'Timestamp of junior employee report deletion (optional)';
COMMENT ON COLUMN udr.junior_report.deleted_by IS 'ID of the user who deleted the junior employee report (optional)';
-- Permission
INSERT INTO sec.perm (permission,description) VALUES
	 ('admin_junior_reg','View and modify any record in juniors registry'),
	 ('access_junior_reg','Has access to junior registry. See only juniors if mentor or was mentor')
	 on conflict do nothing;
INSERT INTO sec."role" ("role", description) VALUES
('mentor', 'Can be assigned as mentor for junior employee') ON CONFLICT DO NOTHING;

INSERT INTO sec.role_perm (role,permission) VALUES
	 ('global_admin','access_junior_reg'),
	 ('global_admin','admin_junior_reg'),
	 ('hr','access_junior_reg'),
	 ('hr','admin_junior_reg'),
	 ('pm','access_junior_reg'),
	 ('mentor','access_junior_reg')
	  on conflict do nothing;

COMMENT ON COLUMN history.history.entity_type IS '
  [empl_manager] - Entity type,
  [working_days] - Working Days Calendar,
  [timesheet_record] - Timesheet Record
  [support_request_group] - Support Request Group
  [support_request] - Support Request
  [junior_registry] - Juniors registry
  ';