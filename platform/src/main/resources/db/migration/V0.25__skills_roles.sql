if not exists (select * from sec_perm where permission='edit_skills')
    begin
        insert into sec_perm(permission, description) values ('edit_skills',
            'Add/Delete employee skills of managed project/department');
        insert into sec_role_perm(role,permission) values ('global_admin','edit_skills');
        insert into sec_role_perm(role,permission) values ('pm','edit_skills');
        insert into sec_role_perm(role,permission) values ('hr','edit_skills');
end