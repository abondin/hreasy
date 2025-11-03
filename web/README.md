# hreasy-web - Internal company platform for HR and employees

@author: Alexander Bondin 2020-2023

## Frameworks

- **States** - vuex
- **Route** - vue-router
- **UI** - vuetify
- **i18n** - vue-i18n + vue-i18n-extract + custom `i18n-add-missing-keys` script to add missing i18n keys to the locale files

## Prerequisites

- Node.js 20.x LTS (tested locally on 20.11). Older runtimes (≤18) are no longer supported for migration tooling.
- npm 10.x (ships with Node 20). Use `npm ci` for reproducible installs.
- Optional: Docker engine 24+ to run the backend locally (`devops/` scripts assume recent Docker CLI).
- Ensure `BACKEND_API_BASE_URL` points to a reachable API host before starting the dev server (see below).

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

## Vue 3 migration sandbox

For the in-progress Vue 3/Vite migration use the separate skeleton located in `migration/vue3-skeleton`.

```shell
cd migration/vue3-skeleton
npm install
# API request proxy (url to backend)
export VITE_DEV_SERVER_PROXY=http://localhost:8081
# API request base url (prefix for axios requests)
export VITE_API_BASE_URL=/api/
npm run dev
```

The sandbox currently exposes a simplified layout, router, and Vuetify 3 theme so that individual features can be ported incrementally from the legacy Vue 2 application.

## Run in docker

- Build docker image `hreasyweb`
In `devops` build docker image and then run it
```shell script
export CI_DEPLOY_TAG=test
./build.sh
docker run -e HREASY_API_HOST=$BACKEND_HOST -p8080:80 --name hreasyweb hreasyweb:test
```

**Important**: <<$BACKEND_HOST>> - hostname and probably port of your `hreasplatform` backend. 
