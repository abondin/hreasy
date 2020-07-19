if not exists (select * from sec_role_perm where role='global_admin' and permission='vacation_view')
begin
    insert into sec_role_perm(role, permission) values ('global_admin', 'vacation_view');
    insert into sec_role_perm(role, permission) values ('global_admin', 'vacation_edit');
end
GO

if not exists (select * from sec_role_perm where role='hr' and permission='vacation_view')
begin
    insert into sec_role_perm(role, permission) values ('hr', 'vacation_view');
    insert into sec_role_perm(role, permission) values ('hr', 'vacation_edit');
end
GO
