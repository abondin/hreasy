if not exists (select * from sysobjects where name='department' and xtype='U')
CREATE TABLE department (
	id int IDENTITY(0,1) NOT NULL PRIMARY KEY,
	name nvarchar(255) NULL
) GO

if not exists (select * from sysobjects where name='dict_level' and xtype='U')
CREATE TABLE dict_level (
	id int IDENTITY(0,1) NOT NULL PRIMARY KEY,
	name nvarchar(255) NULL,
	weight int
) GO

if not exists (select * from sysobjects where name='dict_position' and xtype='U')
CREATE TABLE dict_position (
	id int IDENTITY(1,1) NOT NULL PRIMARY KEY,
	name nvarchar(255) NULL
) GO


if not exists (select * from sysobjects where name='dict_vacancy_position' and xtype='U')
CREATE TABLE dict_vacancy_position (
	id int IDENTITY(1,1) NOT NULL PRIMARY KEY,
	name nvarchar(255) NULL,
	description nvarchar(255) NULL
) GO

if not exists (select * from sysobjects where name='dict_vacancy_priority' and xtype='U')
CREATE TABLE dict_vacancy_priority (
	id int IDENTITY(1,1) NOT NULL PRIMARY KEY,
	priority int NULL,
	name nvarchar(255) NULL
) GO