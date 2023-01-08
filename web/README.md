# hreasy-web - Internal company platform for HR and employees

@author: Alexander Bondin 2020-2023

## Frameworks

- **States** - vuex
- **Route** - vue-router
- **UI** - vuetify
- **i18n** - vue-i18n + vue-i18n-extract + custom `i18n-add-missing-keys` script to add missing i18n keys to the locale files

Run dev server (with connection to backend on localhost).
```shell
# Backend started in docker container with name "hreasyplatform"
# >> export BACKEND_API_BASE_URL=http://hreasyplatform
# Backend started from IDE on localhost
export BACKEND_API_BASE_URL=http://localhost:8081
npm run serve
```

Add missing i18n keys to the locale files:
```sh
npm run i18n-add-missing-keys
```

## Run in docker

- Build docker image `hreasyweb`
In `devops` folder execute `build.sh`
- Run docker container
```shell script
docker run hreasyweb -e HREASY_API_HOST=<<API_HOST_NAME>> -p8080:80 --name hreasyweb
```

**Important**: <<API_HOST_NAME>> - hostname of your `hreasplatform` backend. Backend must be started on 80 port 

