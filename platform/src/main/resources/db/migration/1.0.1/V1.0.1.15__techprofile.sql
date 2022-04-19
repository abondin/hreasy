CREATE SCHEMA IF NOT EXISTS techprofile;

CREATE SEQUENCE IF NOT EXISTS techprofile.TECHPROFILE_LOG_ID_SEQ;
CREATE TABLE IF NOT EXISTS techprofile.techprofile_log (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('techprofile.TECHPROFILE_LOG_ID_SEQ'),
	employee integer NOT NULL,
	filename varchar(1024) NULL,
	created_at timestamp with time zone NOT NULL,
	created_by integer NOT NULL,
	deleted_at timestamp with time zone NULL,
	deleted_by integer NULL
);

COMMENT ON TABLE techprofile.techprofile_log IS 'Log of techprofile files upload';
COMMENT ON COLUMN techprofile.techprofile_log.id IS 'Primary key';
COMMENT ON COLUMN techprofile.techprofile_log.employee IS 'Link to employee (owner of techprofile)';
COMMENT ON COLUMN techprofile.techprofile_log.filename IS 'Uploaded filename (file stores in file system)';
COMMENT ON COLUMN techprofile.techprofile_log.created_at IS 'Created at';
COMMENT ON COLUMN techprofile.techprofile_log.created_by IS 'Created by (link to employee)';
COMMENT ON COLUMN techprofile.techprofile_log.deleted_at IS 'Deleted at';
COMMENT ON COLUMN techprofile.techprofile_log.deleted_by IS 'Deleted by (link to employee)';


-- Permission to download and upload tech profiles
INSERT INTO sec.perm (permission,description) VALUES
	 ('techprofile_download','Download employee tech profile');
INSERT INTO sec.role_perm (role,permission) VALUES
	 ('global_admin','techprofile_download'),
	 ('pm','techprofile_download'),
	 ('hr','techprofile_download');

INSERT INTO sec.perm (permission,description) VALUES
	 ('techprofile_upload','Upload employee tech profile');
INSERT INTO sec.role_perm (role,permission) VALUES
	 ('global_admin','techprofile_upload'),
	 ('pm','techprofile_upload'),
	 ('hr','techprofile_upload');
