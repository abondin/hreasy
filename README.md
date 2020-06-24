# HREasy - HR Portal for STM Internal Uses

@author Alexander Bondin 2019-2020

# Key features

 - Auth with LDAP
 - Show all employees
 - Comming soon...

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


