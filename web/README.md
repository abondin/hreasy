# HR Easy Web

## Current layout

- Vue 3 (main project): root directory (`./`)
- Vue 2 (legacy project): `legacy/vue2/`

## Prerequisites

- Node.js 20.x LTS
- npm 10.x

## Vue 3 (main) - local run

```shell
npm ci
export VITE_DEV_SERVER_PROXY=http://localhost:8081
export VITE_API_BASE_URL=/api/
npm run dev
```

## Vue 2 (legacy) - local run

```shell
cd legacy/vue2
npm ci
export BACKEND_API_BASE_URL=http://localhost:8081
npm run serve
```

## Docker build (both frontends)

Container serves:

- Vue 2 at `/`
- Vue 3 at `/new/`

```shell
cd devops
export CI_DEPLOY_TAG=test
./build.sh
docker run -e HREASY_API_HOST=$BACKEND_HOST -p8080:80 --name hreasyweb hreasyweb:test
```
