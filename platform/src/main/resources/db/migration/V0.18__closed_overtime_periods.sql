if not exists (select * from sysobjects where name='overtime_closed_period' and xtype='U')
begin
    create TABLE overtime_closed_period (
	period int PRIMARY KEY,
    closed_by int NOT NULL,
    closed_at datetimeoffset NOT NULL,
	comment ntext NULL
    );

    alter table overtime_closed_period add CONSTRAINT overtime_closed_period_closed_by_FK
    FOREIGN KEY (closed_by) REFERENCES employee(id);


    create TABLE overtime_period_history (
    id int IDENTITY(1,1) PRIMARY KEY,
	period int NOT NULL,
	action SMALLINT NOT NULL,
    updated_by int NOT NULL,
    updated_at datetimeoffset NOT NULL,
	comment ntext NULL
    );
end

if not exists (select * from sec_perm where permission='overtime_admin')
    begin
        insert into sec_perm(permission, description) values ('overtime_admin',
            'Admin overtime configuration. Close overtime period and other stuff');
        insert into sec_role_perm(role,permission) values ('global_admin','overtime_admin');
end

GO


