if not exists (select * from sec_perm where permission='update_current_project')
begin
    insert into sec_perm(permission, description) values ('update_current_project',
        'Update current project for employee from my projects or my departments');
    insert into sec_role_perm(role,permission) values ('pm','update_current_project');
end