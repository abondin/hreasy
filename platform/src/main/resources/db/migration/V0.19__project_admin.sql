if not exists (select * from sysobjects where name='project_history' and xtype='U')
begin
    create TABLE project_history (
	history_id int PRIMARY KEY,
	project_id int NOT NULL,
	name nvarchar(255) NULL,
    start_date datetime NULL,
    end_date datetime NULL,
    customer nvarchar(255) NULL,
    person_of_contact int NULL,
    updated_by int NOT NULL,
    updated_at datetimeoffset NOT NULL,
	comment ntext NULL
    );

    alter table project_history add CONSTRAINT project_history_project_FK
    FOREIGN KEY (project_id) REFERENCES project(id);

    ALTER TABLE project ADD created_at datetimeoffset NULL;
    ALTER TABLE project ADD created_by int NULL;
end

if not exists (select * from sec_perm where permission='update_project')
    begin
        insert into sec_perm(permission, description) values ('update_project',
            'Admin permission to update information of any project');
        insert into sec_role_perm(role,permission) values ('global_admin','update_project');

        insert into sec_perm(permission, description) values ('create_project',
            'Admin permission to create information of any project');
        insert into sec_role_perm(role,permission) values ('global_admin','create_project');
        insert into sec_role_perm(role,permission) values ('pm','create_project');


        insert into sec_perm(permission, description) values ('project_admin_area',
            'Access to project admin area in UI');
        insert into sec_role_perm(role,permission) values ('global_admin','project_admin_area');
        insert into sec_role_perm(role,permission) values ('pm','project_admin_area');
end
