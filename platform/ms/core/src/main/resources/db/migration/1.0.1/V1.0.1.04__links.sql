-- Separate migration to link entities with cycle dependencies.
-- For instance ...project->business_account->employee->project...

ALTER TABLE ba.business_account ADD CONSTRAINT ba_responsible_employee FOREIGN KEY (responsible_employee) REFERENCES empl.employee (id);
ALTER TABLE ba.business_account ADD CONSTRAINT ba_created_by FOREIGN KEY (created_by) REFERENCES empl.employee (id);
ALTER TABLE proj.project ADD CONSTRAINT project_created_by FOREIGN KEY (created_by) REFERENCES empl.employee (id);