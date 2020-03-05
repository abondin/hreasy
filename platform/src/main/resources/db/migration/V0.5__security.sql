if not exists (select * from sysobjects where name='sec_user' and xtype='U')
begin
-- Security User. No additional information to store for first implementation
CREATE TABLE sec_user (
	id int IDENTITY(1,1) PRIMARY KEY,
	email nvarchar(255) NOT NULL UNIQUE,
	employee_id int
)
ALTER TABLE sec_user ADD CONSTRAINT employee_FK FOREIGN KEY (employee_id) REFERENCES employee(id)

-- User's roles
CREATE TABLE sec_user_role (
	user_id int NOT NULL,
	role nvarchar(255) NOT NULL,
	primary key (user_id, role)
)
ALTER TABLE sec_user_role ADD CONSTRAINT user_FK FOREIGN KEY (user_id) REFERENCES sec_user(id)


-- Role's permissions
CREATE TABLE sec_role_perm (
	id int IDENTITY(1,1) PRIMARY KEY,
	role nvarchar(255) NOT NULL,
	permission nvarchar(255) NOT NULL
)

end
GO

