DELETE FROM sec.role_perm WHERE permission = 'update_project';
INSERT INTO sec.role_perm (role, permission) VALUES
	 ('pm', 'update_project');
UPDATE sec.perm SET description='Admin project if employee is project/ba/department manager'
    WHERE permission='update_project';

INSERT INTO sec.perm (permission,description) VALUES
	 ('update_project_globally','Admin permission to update information of any project');
INSERT INTO sec.role_perm (role, permission) VALUES
   	 ('global_admin', 'update_project_globally');

