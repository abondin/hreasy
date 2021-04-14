if not exists (select * from sysobjects where name='skill_group' and xtype='U')
begin
    create TABLE skill_group (
    id int IDENTITY(1,1) PRIMARY KEY,
	name nvarchar(256) NOT NULL,
	description nvarchar(4000) NULL,
	archived bit NOT NULL DEFAULT 0,
	CONSTRAINT skill_group_UK UNIQUE(name)
	);
end
go

if not exists (select * from sysobjects where name='skill' and xtype='U')
begin
    create TABLE skill (
	id int IDENTITY(1,1) PRIMARY KEY,
	employee_id int NOT NULL,
	group_id int NULL,
	name nvarchar(256) NULL,
	shared bit NOT NULL default 0,
    created_by int NOT NULL,
    created_at datetimeoffset NOT NULL,
    deleted_by int NULL,
    deleted_at datetimeoffset NULL,
    CONSTRAINT skill_UK UNIQUE(employee_id,group_id,name)
   );

    alter table skill add CONSTRAINT skill_empl_FK
    FOREIGN KEY (employee_id) REFERENCES employee(id);

    alter table skill add CONSTRAINT skill_group_FK
    FOREIGN KEY (group_id) REFERENCES skill_group(id);

end

if not exists (select * from sysobjects where name='skill_rating' and xtype='U')
begin
    create TABLE skill_rating (
	id int IDENTITY(1,1) PRIMARY KEY,
	skill_id int NOT NULL,
	rating real NOT NULL,
	notes nvarchar(4000) NULL,
    created_by int NOT NULL,
    created_at datetimeoffset NOT NULL,
    updated_at datetimeoffset NOT NULL,
    deleted_by int NULL,
    deleted_at datetimeoffset NULL,
    CONSTRAINT skill_rating_UK UNIQUE(skill_id,created_by)
   );

    alter table skill_rating add CONSTRAINT skill_rating_skill_FK
    FOREIGN KEY (skill_id) REFERENCES skill(id);
end