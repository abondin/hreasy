# Core Microservice

Handles 100% of business logic

- Login, authentication and authorization - should be moved to `apifacade` and probably to `security-ms`
- Roles and permissions - should be moved to new `security-ms`
- Employee Profile, Employee List, Employee Admin - should be moved to new `employee-ms`
- Vacations - should be moved to new `vacation-ms`
- Overtimes - should be moved to new `overtime-ms`
- Dictionaries
- Projects and business accounts
- Assessments - should be moved to new `assessment-ms`
- Notifications (messages in UI) - In the middle of transfer to `notification-ms`
- Messages and background jobs - In the middle of transfer to `notification-ms`
