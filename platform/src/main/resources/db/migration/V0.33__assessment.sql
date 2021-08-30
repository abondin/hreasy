if not exists (select * from sysobjects where name='assessment' and xtype='U')
begin
    CREATE TABLE assessment (
        id int IDENTITY(1,1) PRIMARY KEY,
        employee int NOT NULL,
        planned_date datetime NOT NULL,
        created_at datetimeoffset NOT NULL,
        created_by int NOT NULL,
        completed_at datetimeoffset NULL,
        completed_by int NULL,
        canceled_at datetimeoffset NULL,
        canceled_by int NULL
    );

    alter table assessment add CONSTRAINT assessment_employee_FK FOREIGN KEY (employee) REFERENCES employee(id);
    alter table assessment add CONSTRAINT assessment_created_FK FOREIGN KEY (created_by) REFERENCES employee(id);
    alter table assessment add CONSTRAINT assessment_completed_FK FOREIGN KEY (completed_by) REFERENCES employee(id);
    alter table assessment add CONSTRAINT assessment_canceled_FK FOREIGN KEY (canceled_by) REFERENCES employee(id);


    CREATE TABLE assessment_form (
        id int IDENTITY(1,1) PRIMARY KEY,
        assessment_id int NOT NULL,
        owner int NOT NULL,
        -- 1 - self assessment, 2 - manager feedback, 3 - meeting notes, 4 - conclusion and decision
        form_type int NOT NULL,
        content ntext NULL,
        completed_at datetimeoffset NULL,
        completed_by int NULL
    );

    alter table assessment_form add CONSTRAINT assessment_form_assessment_FK FOREIGN KEY (assessment_id) REFERENCES assessment(id);
    alter table assessment_form add CONSTRAINT assessment_form_owner_FK FOREIGN KEY (owner) REFERENCES employee(id);
    alter table assessment_form add CONSTRAINT assessment_form_completed_FK FOREIGN KEY (completed_by) REFERENCES employee(id);


    CREATE TABLE assessment_form_history (
        id int IDENTITY(1,1) PRIMARY KEY,
        assessment_form_id int NOT NULL,
        content ntext NULL,
        updated_at datetimeoffset NOT NULL,
        updated_by int NOT NULL
    );

    CREATE TABLE assessment_form_template (
        form_type int NOT NULL,
        content ntext NULL,
        CONSTRAINT PK__assessment_form_template PRIMARY KEY (form_type)
    );

    CREATE TABLE assessment_form_template_history (
        id int IDENTITY(1,1) PRIMARY KEY,
        form_type int NOT NULL,
        content ntext NULL,
        updated_at datetimeoffset NOT NULL,
        updated_by int NOT NULL
    );

end
GO


if not exists (select * from sec_perm where permission='view_assessment_full')
    begin
        insert into sec_perm(permission,description) values ('view_assessment_full','View all assessment forms without restrictions');
        insert into sec_perm(permission,description) values ('create_assessment','Schedule new assessment and invite managers');
        insert into sec_role_perm(role,permission) values ('global_admin','view_assessment_full');
        insert into sec_role_perm(role,permission) values ('global_admin','create_assessment');
        insert into sec_role_perm(role,permission) values ('hr','view_assessment_full');
        insert into sec_role_perm(role,permission) values ('hr','create_assessment');
        insert into sec_role_perm(role,permission) values ('pm','create_assessment');
    end
GO

if not exists (select * from assessment_form_template)
    begin
        insert into assessment_form_template (form_type, content) values
        (1, '{"groups":[{"key":"general","displayName":"General","fields":[{"key":"performance","displayName":"Employee Performance","type":"short_text_with_rate"},{"key":"skill_level","displayName":"Technical Skill Level","type":"short_text_with_rate"}]},{"key":"project","displayName":"Review of the project","fields":[{"key":"teamwork","displayName":"Team work (comfort, efficiency)","type":"short_text_with_rate"},{"key":"manager","displayName":"Manager feedback","type":"short_text_with_rate"}]},{"key":"softs_kills","displayName":"Soft Skill","fields":[{"key":"teamwork","displayName":"Team work (comfort, efficiency)","type":"text"},{"key":"manager","displayName":"Manager feedback","type":"text"}]}]}'),
        (2, '{"groups":[{"key":"general","displayName":"General","fields":[{"key":"performance","displayName":"Employee Performance","type":"short_text_with_rate"},{"key":"skill_level","displayName":"Technical Skill Level","type":"short_text_with_rate"},{"key":"open_comment","displayName":"Open Comment","type":"text"}]}]}'),
        (3, '{"groups":[{"key":"general","displayName":"General","fields":[{"key":"open_comment","displayName":"Open Comment","type":"text"}]}]}'),
        (4, '{"groups":[{"key":"general","displayName":"General","fields":[{"key":"open_comment","displayName":"Open Comment","type":"text"}]}]}');
    end
go