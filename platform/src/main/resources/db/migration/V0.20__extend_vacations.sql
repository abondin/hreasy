-- Planned dates (not editable)
alter table vacation add planned_start_date datetime NULL;
alter table vacation add planned_end_date datetime NULL;

-- Number of calendar days of the vacation
alter table vacation add days_number int null;

-- 0 planning
-- 1 taken
alter table vacation add stat int;

alter table vacation add documents nvarchar(255);

alter table vacation add created_at datetimeoffset;
alter table vacation add created_by int;

alter table vacation add updated_at datetimeoffset;
alter table vacation add updated_by int;

alter table vacation add CONSTRAINT vacation_created_by_FK
 FOREIGN KEY (created_by) REFERENCES employee(id);

alter table vacation add CONSTRAINT vacation_updated_by_FK
  FOREIGN KEY (updated_by) REFERENCES employee(id);

GO

update vacation set stat = 0;
alter table vacation alter column stat int not null;


create TABLE vacation_history (
	id int IDENTITY(1,1) PRIMARY KEY,
	vacation_id int,
	created_at datetimeoffset NOT NULL,
	created_by int NOT NULL,
	employee int NULL,
	year int NOT NULL,
	start_date datetime NULL,
	end_date datetime NULL,
	planned_start_date datetime NULL,
	planned_end_date datetime NULL,
	days_number int,
	stat int,
	documents nvarchar(255),
	notes nvarchar(255) NULL,
)

alter table vacation_history add CONSTRAINT vacation_history_vacation_FK
  FOREIGN KEY (vacation_id) REFERENCES vacation(id);