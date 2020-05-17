if not exists (select * from sec_role_perm where role='global_admin')
begin
    insert into sec_role_perm(role, permission) values ('global_admin', 'update_current_project_global');
    insert into sec_role_perm(role, permission) values ('global_admin', 'update_avatar');
end
GO

if not exists (select * from sec_role_perm where role='hr')
begin
    insert into sec_role_perm(role, permission) values ('hr', 'update_current_project_global');
    insert into sec_role_perm(role, permission) values ('hr', 'update_avatar');
end
GO
