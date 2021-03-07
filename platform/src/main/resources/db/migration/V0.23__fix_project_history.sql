-- You have to manually DROP PK constraint (i am too lazy to create a script for PK deletion). Something like
-- ALTER TABLE project_history DROP CONSTRAINT PK__project___096AA2E9E2C279C0 GO
ALTER TABLE project_history ADD temp_id int IDENTITY(1,1);
go
ALTER TABLE project_history DROP COLUMN history_id;
go
Exec sp_rename 'project_history.temp_id', 'history_id', 'Column'
go
ALTER TABLE project_history
ADD CONSTRAINT project_history_PK PRIMARY KEY (history_id);
go

