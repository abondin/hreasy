if not exists (select * from sec_perm where permission='admin_users')
    begin
        insert into sec_perm(permission, description) values ('admin_users',
            'Admin user. Assign roles. Assign accessible projects and departments');
        insert into sec_role_perm(role,permission) values ('global_admin','admin_users');
end