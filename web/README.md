# HR Easy Web

## Repository layout

- Vue 3 app (main): repository root (`./`)
- Vue 2 app (legacy): `legacy/vue2/`
- Legacy docs: `legacy/README.md`
- Main UI stack: Vue 3.5 + Vuetify 4 + Pinia 3 + Vue Router 4

## Prerequisites

- Node.js 20.x LTS
- npm 10.x

## Vue 3 (main) - local development

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
