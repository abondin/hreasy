ALTER TABLE empl.import_workflow
    add column wf_type smallint;
update empl.import_workflow set wf_type = 1;
alter table empl.import_workflow alter column wf_type set not null;

comment on column empl.import_workflow.wf_type is '
Type of workflow:
1 - Import Employees
2 - Import Kids
'
