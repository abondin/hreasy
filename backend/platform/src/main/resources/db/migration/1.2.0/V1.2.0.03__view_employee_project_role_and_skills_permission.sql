INSERT INTO sec.perm (permission,description) VALUES
	 ('view_empl_current_project_role','View current project role');
INSERT INTO sec.perm (permission,description) VALUES
	 ('view_empl_skills','View employee skills');

INSERT INTO sec.role_perm (role, permission) VALUES
   	 ('global_admin', 'view_empl_current_project_role');
INSERT INTO sec.role_perm (role, permission) VALUES
   	 ('pm', 'view_empl_current_project_role');
INSERT INTO sec.role_perm (role, permission) VALUES
   	 ('hr', 'view_empl_current_project_role');

INSERT INTO sec.role_perm (role, permission) VALUES
   	 ('global_admin', 'view_empl_skills');
INSERT INTO sec.role_perm (role, permission) VALUES
   	 ('pm', 'view_empl_skills');
INSERT INTO sec.role_perm (role, permission) VALUES
   	 ('hr', 'view_empl_skills');
