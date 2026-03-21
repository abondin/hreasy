# HR Easy Web

## Repository layout

- Vue 3 app: repository root (`./`)
- Main stack: Vue 3 + Vuetify 4 + Pinia + Vue Router 4 + Vue I18n

## Prerequisites

- Node.js 20.x LTS
- npm 10.x

## Local development

```shell
npm ci
export VITE_DEV_SERVER_PROXY=http://localhost:8081
export VITE_API_BASE_URL=/api/
npm run dev
```

Useful commands:

```shell
npm run type-check
npm run lint
npm run test:unit
npm run test:e2e
npm run build
```

E2E docs:

- [`e2e/README.md`](./e2e/README.md)

## Docker build

Container serves Vue 3 at `/`.

```shell
cd devops
export CI_DEPLOY_TAG=test
./build.sh
docker run -e HREASY_API_HOST=$BACKEND_HOST -p8080:80 --name hreasyweb hreasyweb:test
```
