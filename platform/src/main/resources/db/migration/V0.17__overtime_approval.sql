if not exists (select * from sysobjects where name='overtime_approval_decision' and xtype='U')
begin
    create TABLE overtime_approval_decision (
	id int IDENTITY(1,1) PRIMARY KEY,
    report_id int NOT NULL,
    approver int NOT NULL,
    decision nvarchar(255) NOT NULL,
    decision_time datetimeoffset NOT NULL,
    cancel_decision_time datetimeoffset NULL,
	comment ntext NULL
    );

alter table overtime_approval_decision add CONSTRAINT overtime_approval_decision_report_FK
 FOREIGN KEY (report_id) REFERENCES overtime_report(id);
alter table overtime_approval_decision add CONSTRAINT overtime_approval_decision_approver_FK
 FOREIGN KEY (approver) REFERENCES employee(id);
end
GO