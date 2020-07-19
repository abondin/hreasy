if not exists (select * from sec_role_perm where role='global_admin' and permission='overtime_view')
begin
    insert into sec_role_perm(role, permission) values ('global_admin', 'overtime_view');
    insert into sec_role_perm(role, permission) values ('global_admin', 'overtime_edit');
end
GO

if not exists (select * from sec_role_perm where role='hr' and permission='overtime_view')
begin
    insert into sec_role_perm(role, permission) values ('hr', 'overtime_view');
    insert into sec_role_perm(role, permission) values ('hr', 'overtime_edit');
end
GO
