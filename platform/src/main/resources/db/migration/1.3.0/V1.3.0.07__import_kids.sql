ALTER TABLE empl.import_workflow
    add column wf_type smallint;
update empl.import_workflow set wf_type = 1;
alter table empl.import_workflow alter column wf_type set not null;

drop index if exists empl.active_unique;
CREATE UNIQUE INDEX IF NOT EXISTS import_active_unique ON empl.import_workflow (created_by, wf_type)
    WHERE state in (0,1,2);

comment on column empl.import_workflow.wf_type is '
Type of workflow:
1 - Import Employees
2 - Import Kids
'
