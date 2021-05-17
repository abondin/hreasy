-- articles
if not exists (select * from sysobjects where name='article' and xtype='U')
begin
    create TABLE article (
	id int IDENTITY(1,1) PRIMARY KEY,
	article_group nvarchar(255) NOT NULL,
	name nvarchar(255) NOT NULL,
	description ntext NULL,
	content ntext NOT NULL,
	moderated bit default(0),
	archived bit default(0),
	created_at datetimeoffset NULL,
	created_by int NULL,
	updated_at datetimeoffset NULL,
	updated_by int NULL
    )
    alter table article add CONSTRAINT article_created_FK FOREIGN KEY (created_by) REFERENCES employee(id);
    alter table article add CONSTRAINT article_updated_FK FOREIGN KEY (updated_by) REFERENCES employee(id);

    create TABLE article_history (
    id int IDENTITY(1,1) PRIMARY KEY,
    article_id int NOT NULL,
	article_group nvarchar(255) NOT NULL,
    name nvarchar(255) NOT NULL,
    description ntext NULL,
	content ntext NOT NULL,
    moderated bit default(0),
    archived bit default(0),
    created_at datetimeoffset NULL,
    created_by int NULL,
    updated_at datetimeoffset NULL,
    updated_by int NULL
    )

end
GO

if not exists (select * from sec_perm where permission='edit_articles')
    begin
        insert into sec_perm(permission,description) values ('edit_articles','Create/update and moderate articles and news');
        insert into sec_role(role,description) values ('content_management','Create/update and moderate articles and news');
        insert into sec_role_perm(role,permission) values ('global_admin','edit_articles');
        insert into sec_role_perm(role,permission) values ('content_management','edit_articles');
end