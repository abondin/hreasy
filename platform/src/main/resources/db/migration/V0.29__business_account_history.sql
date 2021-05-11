-- business_account history
if not exists (select * from sysobjects where name='business_account_history' and xtype='U')
begin
    CREATE TABLE hr.dbo.business_account_history (
    	id int IDENTITY(1,1) PRIMARY KEY,
    	ba_id int NOT NULL,
    	name nvarchar(255)  NOT NULL,
    	responsible_employee int NULL,
    	description ntext NULL,
    	archived bit default(0),
    	updated_at datetimeoffset NULL,
    	updated_by int NULL
    );
    
    CREATE TABLE hr.dbo.ba_position_history (
    	id int IDENTITY(1,1) PRIMARY KEY,
    	ba_position_id int NOT NULL,
    	business_account int NOT NULL,
    	name nvarchar(255)  NOT NULL,
    	rate decimal(19,4) NOT NULL,
    	description ntext  NULL,
    	archived bit default(0),
    	updated_at datetimeoffset NULL,
    	updated_by int NULL
    );

    CREATE TABLE hr.dbo.ba_assignment_history (
    	id int IDENTITY(1,1) PRIMARY KEY,
    	ba_assignment_id int NOT NULL,
    	business_account int NOT NULL,
    	employee int NULL,
    	project int NULL,
    	parent_assignment int NULL,
    	ba_position int NULL,
    	employment_rate real NULL,
    	ba_position_rate real NULL,
    	comment ntext  NULL,
    	start_date datetime NULL,
    	end_date datetime NULL,
    	planned_start_date datetime NULL,
    	planned_end_date datetime NULL,
    	archived bit default(0),
    	closed_at datetimeoffset NULL,
    	closed_by int NULL,
    	closed_reason nvarchar(255)  NULL,
    	closed_comment ntext  NULL,
    	updated_at datetimeoffset NULL,
    	updated_by int NULL
    );
end
GO
