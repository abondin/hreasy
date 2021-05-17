# HREasy - HR Portal for STM Internal Uses

@author Alexander Bondin 2019-2021

# Key features

 - Auth with LDAP
 - Show all employees
 - View/Add/Approve Overtimes
 - View/Add/Update Vacations
 - Admin projects dictionary
 - Admin user roles

# Projects

 - portal - monolite backend
 - web - Vue JS Single Page Application

# Technologies Stack

 - ~~Kotlin~~ Java on backend
 - SQL Server as database
 - Spring Reactive
 - Vue + vuetifyjs + ts on frontend
 
# Local build

```shell script
export DOCKER_REPOSITORY=<Your Repository> (optional)
export CI_DEPLOY_TAG=latest (optional)
./devops/build.sh
```

# Local run in Docker-compose

Docker Compose file is located in `.hreasy-localdev`

```shell script
cd .hreasy-localdev
docker-compose up -d
```

*Tip* The following command updates single service

```shell script
docker-compose up -d --no-deps --force-recreate --build hreasyplatform
``` 

# Permissions and roles

![Security Database](./.architecture/hr_sec.png "Security Database Scheme")

Security model based on two main entities:

1) Employee - key entity of the whole project. Describe company employee
2) User - employee projection in security scheme. Currently we consider that *User = Employee*. Also we have the
   assumption that user and employee are always associated via email. (implicit dependency)
   Entity "User" designed for future features when we have portal user without employee projection. It might be system
   account or some kind of portal admin.

User (more accurately employee) may have access to the actions for employees, currently assigned to the specific
project. Or to the actions for employees, currently assigned to any project from specific department. Please take a look
on `employee_accessible_departments` and `employee_accessible_projects` tables respectively. Permissions depended
on `employee_accessible_departments` and `employee_accessible_projects` marked in the table bellow.

Many permissions are always allowed the the currently logged in employee. Also marked in the permission table.

*For example*: we have employee *John* with accessible project *Project1* and role with permission *overtime_view*. Also
we have employee *Dave* without any role currently assigned to the *Project1*. In that case John can see his overtimes
and Dave's overtimes. Dave can see only his own overtimes.

- List of permissions are code based. Database table must be always in sync with backend and frontend code.
- Set of permissions combined to the role. List of roles is code independent and can be updated in database (and in
  admin UI in future releases). See `sec_role` and `sec_role_perm` tables.
- Roles assigned to the user in `sec_user_role` table (and in admin UI in future releases).

**List of supported permissions**:

|permission|depends on department|always available to oneself |description|
|----|------|------|------|
|update_current_project_global|N|N|Change current employee project|
|update_current_project|Y|N|Update current project for employee from my projects or my departments|
|update_avatar|N|Y|Update employee avatar|
|overtime_view|N|Y|View overtimes of given employee|
|overtime_edit|Y|Y|Edit and approve overtimes of given employee|
|overtime_admin|N|N|Admin overtime configuration. Close overtime period and other stuff|
|vacation_view|Y|Y|View vacations of given employee|
|vacation_edit|Y|Y|View vacations of given employee|
|project_admin_area|N|N|Access to project admin area in UI|
|update_project|N|N|Admin permission to update information of any project. Without this permission user can update only projects created by himself|
|create_project|N|N|Admin permission to create new project|
|admin_users|N|N|Admin user. Assign roles. Assign accessible projects and departments|
|edit_skills|Y|Y|Add/Delete employee skills of managed project/department|
|edit_business_account|N|N|Add/Update business account and create/update BA positions|
|assign_to_ba_position|Y|N|Assign employee to business account position|
|edit_articles|N|N|Create, update and moderate articles and news|

**Default permissions and roles**

|role|description|
|----|------|
|global_admin|Full access|
|hr|Add/Update/Fire all employees|
|pm|Overtime and vacation review and update, reasign empoyee between managed projects|
|finance|Work with business account positions and expenses|
|content_management|Create, update and moderate articles and news|

|permissions|roles|
|----|------|
|update_current_project_global|global_admin, hr|
|update_current_project|global_admin, hr, pm|
|update_avatar|global_admin, hr|
|overtime_view|global_admin, hr, pm|
|overtime_edit|global_admin, hr, pm|
|overtime_admin|global_admin|
|vacation_view|global_admin, hr, pm|
|vacation_edit|global_admin, hr, pm|
|project_admin_area|global_admin,pm|
|update_project|global_admin|
|create_project|global_admin, pm|
|admin_users|global_admin|
|edit_skills|global_admin,pm,hr|
|edit_business_account|global_admin,finance|
|assign_to_ba_position|global_admin,pm,finance|
|edit_articles|global_admin,content_management|

# Articles

HR Easy uses markdown to show articles (news and shared information).
To add new article you should just add new markdown file to `<hreasy.fs.resource-path>/article/shared` folder.