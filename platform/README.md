# HREasy - HR Portal for STM Internal Uses

@author Alexander Bondin 2019-2020

# Technologies Stack

 - SQL Server as database
 - Spring Reactive


# Run locally
 - ./localdev - contains docker-compose to start 3pty components (Microsoft SQL Server)
 - `*.hr` hosts are using in application configuration by default. Please locate it to your docker IP address in `hosts` file
   - `10.0.75.1 sql.hr`
 - LDAP - HREasy based to LDAP authentication. It is important to correctly specify LDAP attributes in application.yaml
 - Activate DEV Spring Boot profile `-Dspring.profiles.active=dev` to enable CORS from localhost:8080 and other DEV stuff

# Run in Docker

 - In folder `devops` execute `./build.sh`. New docker image will be created (script prints the image name). For example `hreasyplatform:latest`
 - Run docker container. For example run development configuration
 ```shell script
docker run -d -e SPRING.PROFILES.ACTIVE=dev --name hreasyplatform -p8081:8081 hreasyplatform:latest
```

# Testing

<aside class="warning">
Docker Installer required
</aside>

[Testcontainers](https://www.testcontainers.org) uses to run SQL Server Docker Container for tests.
You may use your own container by setting VM option
 
`-Dhreasy.test.existing-database-docker=true` 

To clean-up database before tests set VM flyway commands

`-Dhreasy.db.flyway-commands=clean,migrate`

Flyway commands also can be executed in maven:
`mvn flyway:migrate -Dflyway.user=sa -Dflyway.password=HREasyPassword2019! -Dflyway.url=jdbc:sqlserver://sql.hr:1433`

# Permissions and roles

![Security Database](./.architecture/hr_sec.png "Security Database Scheme")

Security model based on two main entities:
1) Employee - key entity of the whole project. Describe company employee
2) User - employee projection in security scheme.
Currently we consider that *User = Employee*.
Also we have the assumption that user and employee are always associated via email. (implicit dependency)
Entity user designed for future features when we have portal user without employee projection.
It might be system account or some kind of portal admin. 

User (more accurately employee) may have access to the actions for employees, currently assigned to the specific project.
Or to the actions for employees, currently assigned to any project from specific department.
Please take a look on `employee_accessible_departments` and `employee_accessible_projects` tables respectively.
Permissions depended on `employee_accessible_departments` and `employee_accessible_projects` marked in the table bellow.
   
Many permissions are always allowed the the currently logged in employee. Also marked in the permission table.

*For example*: we have employee *John* with accessible project *Project1* and role with permission *overtime_view*.
Also we have employee *Dave* without any role currently assigned to the *Project1*.
In that case John can see his overtimes and Dave's overtimes. Dave can see only his own overtimes.        
   
- List of permissions are code based. Database table must be always in sync with backend and frontend code.
- Set of permissions combined to the role.
List of roles is code independent and can be updated in database (and in admin UI in future releases). 
See `sec_role` and `sec_role_perm` tables.
- Roles assigned to the user in `sec_user_role` table (and in admin UI in future releases). 

**List of supported permissions**:

|permission|depends on department|always available to oneself |description|
|----|------|------|------|
|update_current_project_global|N|N|Change current employee project|
|update_avatar|N|Y|Update employee avatar|
|overtime_view|Y|Y|View overtimes of given employee|
|overtime_edit|Y|Y|View overtimes of given employee|
|vacation_view|Y|Y|View overtimes of given employee|
|vacation_edit|Y|Y|View overtimes of given employee|

**Default permissions and roles**


|role|description|
|----|------|
|global_admin|Full access|
|hr|Add/Update/Fire all employees|
|pm|Overtime and vacation review and update|

|permissions|roles|
|----|------|
|update_current_project_global|global_admin, hr|
|update_avatar|global_admin, hr|
|overtime_view|global_admin, hr, pm|
|overtime_edit|global_admin, hr, pm|
|vacation_view|global_admin, hr, pm|
|vacation_edit|global_admin, hr, pm|


** Default Test Data for Unit Tests **

|Employee|Current Assigned Project|Roles|Accessible Departments|Accessible Projects|
|--------|---------------|-----|----------------------|-------------------|
|Haiden Spooner|M1 Billing|-|-|-|
|Asiyah Bob|M1 Billing|-|-|-|
|Maxwell May|M1 Billing|-|-|M1 Billing|
|Percy Gough|-|-|Development|-|
|Ammara Knott|M1 FMS|-|-|-|
|Jenson Curtis|M1 FMS|-|-|-|
|Jawad Mcghee|-|-|-|M1 FMS|
|Amy Beck|M1 Policy Manager|-|-|-|
|Kyran Neville|-|-|-|M1 Billing,M1 FMS,M1 Policy Manager|
|Jonas Martin|M1 ERP Integration|-|-|-|
|Toby Barrow|-|-|Development,Integration|-|
|Maysa Sheppard|-|hr|-|-|
|Shaan Pitts|-|global_admin|-|-|
