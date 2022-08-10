------   ADD description and responsible_employees to the project
ALTER TABLE proj.project ADD COLUMN description TEXT NULL;
ALTER TABLE proj.project ADD COLUMN responsible_employees JSONB;

COMMENT ON COLUMN proj.project.description IS 'Detailed description of the project in markdown format';
COMMENT ON COLUMN proj.project.responsible_employees IS 'Responsible employees array with additional meta
[
    {
      "employee": {
        "id": 123,
        "name": "Ivanov Ivan"
      }
      "types": ["technical"]
    },
    {
      "employee": {
        "id": 321,
        "name": "Petrov Petr"
      }
      "types": ["technical", "organization"]
    },
]
';

ALTER TABLE proj.project_history ADD COLUMN description TEXT NULL;
ALTER TABLE proj.project_history ADD COLUMN responsible_employees JSONB NULL;

COMMENT ON COLUMN proj.project_history.description IS 'Detailed description of the project in markdown format';
COMMENT ON COLUMN proj.project_history.responsible_employees IS 'Responsible employees array with additional meta. See proj.project.responsible_employees for the details';

------   ADD responsible_employees to the business accounts
ALTER TABLE ba.business_account ADD COLUMN responsible_employees JSONB;

WITH employees AS (
    select id, trim(concat_ws(' ', e.lastname, e.firstname, e.patronymic_name)) displayName
    from empl.employee e
)
UPDATE ba.business_account ba_to_update set responsible_employees =
 JSONB('[{"employee":{"id":'||employees.id||',"name":"'||employees.displayName||'"}, "types":["technical", "organization"]}]')
 from ba.business_account as ba
 join employees on ba.responsible_employee=employees.id
 where ba_to_update.id=ba.id;


ALTER TABLE ba.business_account DROP COLUMN responsible_employee;

------------ ADD responsible_employees to the business accounts history

ALTER TABLE ba.business_account_history ADD COLUMN responsible_employees JSONB;

WITH employees AS (
    select id, trim(concat_ws(' ', e.lastname, e.firstname, e.patronymic_name)) displayName
    from empl.employee e
)
UPDATE ba.business_account_history ba_to_update set responsible_employees =
 JSONB('[{"employee":{"id":'||employees.id||',"name":"'||employees.displayName||'"}, "types":["technical", "organization"]}]')
 from ba.business_account_history as ba
 join employees on ba.responsible_employee=employees.id
 where ba_to_update.id=ba.id;


ALTER TABLE ba.business_account_history DROP COLUMN responsible_employee;

------------------------------

COMMENT ON COLUMN ba.business_account.description IS 'Detailed description of the business account in markdown format';
COMMENT ON COLUMN ba.business_account.responsible_employees IS 'Responsible employees array with additional meta. See proj.project.responsible_employees for the details';

COMMENT ON COLUMN ba.business_account_history.description IS 'Detailed description of the business account in markdown format';
COMMENT ON COLUMN ba.business_account_history.responsible_employees IS 'Responsible employees array with additional meta. See proj.project.responsible_employees for the details';
