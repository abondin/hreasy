# Legacy Frontend (Vue 2)

This directory contains the legacy HR Easy frontend.

- Source code: `legacy/vue2/`
- Stack: Vue 2.7, Vue Router 3, Vuex 3, Vuetify 2, Vue CLI

## Prerequisites

- Node.js 20.x LTS
- npm 10.x

## Local run

```shell
cd legacy/vue2
npm ci
export BACKEND_API_BASE_URL=http://localhost:8081
npm run serve
```

## Main commands

```shell
cd legacy/vue2
npm run build
npm run test:unit
npm run lint
npm run i18n-add-missing-keys
```

## Notes

- Legacy app is kept for production parity while pages are migrated to Vue 3.
- New feature development should go to the root Vue 3 app unless explicitly required in legacy.
