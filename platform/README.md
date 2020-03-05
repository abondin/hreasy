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

