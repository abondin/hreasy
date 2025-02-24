DROP VIEW IF EXISTS empl.v_employee_project_changes;
CREATE OR REPLACE VIEW empl.v_employee_project_changes AS
WITH project_changes AS (
    SELECT
        eh.id as id,
        eh.employee AS employee_id,
        emp.display_name AS employee_display_name,
        eh.current_project AS project_id,
        p."name" AS project_name,
        eh.current_project_role AS project_role,
        eh.created_at AS changed_at,
        eh.created_by AS changed_by_id,
        changer.display_name AS changed_by_display_name,
        ba.id as ba_id,
        ba.name as ba_name,
        LAG(eh.current_project) OVER (PARTITION BY eh.employee ORDER BY eh.created_at) AS prev_project,
        LAG(eh.current_project_role) OVER (PARTITION BY eh.employee ORDER BY eh.created_at) AS prev_role
    FROM
        empl.employee_history eh
    JOIN
        empl.employee emp ON eh.employee = emp.id
    LEFT JOIN
        proj.project p ON eh.current_project = p.id
    LEFT JOIN
        ba.business_account ba ON p.ba_id = ba.id
    JOIN
        empl.employee changer ON eh.created_by = changer.id
)
SELECT
    id,
    employee_id,
    employee_display_name,
    project_id,
    project_name,
    ba_id,
    ba_name,
    project_role,
    changed_at,
    changed_by_id,
    changed_by_display_name
FROM
    project_changes
WHERE
    (prev_project IS DISTINCT FROM project_id OR prev_role IS DISTINCT FROM project_role)
    OR (prev_project IS NULL AND project_id IS NOT NULL);
