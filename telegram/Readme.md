# Telegram Bot for HR Easy

## Telegram Account Setup Procedure

1. **Profile Configuration:**
    - Employee or admin sets up the Telegram account in their profile within the HR Easy web interface.
2. **Initiate Chat:**
    - The employee starts a chat with the Telegram bot and sends a "/confirm_account" request.
3. **Email Confirmation:**
    - HR Easy sends a confirmation URL to the employee's working email, which is set in HR Easy.
4. **Open Confirmation Link:**
    - The employee opens the provided link in any web browser.
5. **Confirmation Process:**
    - Upon opening the link, the Telegram account is marked as confirmed in HR Easy.

## Supported Commands

- **find**
   - Find an employee by display name, email, or Telegram. Optionally specify project and project role.
      - `/find` - Open instructions.
      - `/find Bondin Alexander` - Find a specific employee.
      - `/find Alexander SuperProject` - Find employees associated with a project.

- **my_profile**
   - Provides basic information about yourself.

- **confirm_account**
   - Initiates the Telegram account confirmation process.

- **support**
    - Post new support request
  
**NOTE:** To post new request you have to manually create support groups in database.
SQL example:
```sql
INSERT INTO "support".support_request_group
("key", display_name, description, "configuration", created_at, created_by, deleted_at, deleted_by)
VALUES (
  'IT',
  'IT Department',
  'Any questions with hardware and software',
  '{"emails": ["Alexander.Bondin@hreasy.ru"],
   "categories": ["Hardware", "Software", "Accounts"]}'::jsonb,
    '2024-07-08 12:09:05.415',
     5, NULL, NULL);
```

## Setup from the scratch

1. Create telegram bot using t.me/BotFather
2. Setup application.yaml section with your values. You can use envoriment variables as well. See [Spring Externalized Configuration](https://docs.spring.io/spring-boot/docs/1.2.2.RELEASE/reference/html/boot-features-external-config.html#boot-features-external-config)
```yaml
hreasy:
  telegram:
    bot-username: "XXX"
    bot-token: "YYY"
    bot-creator: YourAccountId
    platform:
      base_url: "<URL TO HR EASY BACKEND>/telegram/"
```

## Auth and confirmation process in technical details

![Architecture](../.docs/Telegram_auth_and_confirm.drawio.png "Architecture")