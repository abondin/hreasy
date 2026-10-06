# HR Easy Web

## Repository layout

- Vue 3 app: `web/` in the monorepository. Run the commands below from `web/`.
- Main stack: Vue 3 + Vuetify 4 + Pinia + Vue Router 5 + Vue I18n

## Prerequisites

- Node.js 26.x
- npm 11.19.1 (see `engines` and `packageManager` in `package.json`)

## Local development

```shell
npm ci
export VITE_DEV_SERVER_PROXY=http://localhost:8081
export VITE_API_BASE_URL=/api/
npm run dev
```

The dev server runs at `http://localhost:5173` and proxies `/api` to the Platform backend. The backend must be running at the configured proxy address. Bash environment syntax is shown above; in PowerShell use `$env:VITE_DEV_SERVER_PROXY='http://localhost:8081'`.

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
- [`src/e2e-harness/README.md`](./src/e2e-harness/README.md)

Autonomous harness suite:

```shell
npx playwright test e2e/harness --project=chromium
```

Main app without backend, using mocked API responses:

```shell
npx playwright test e2e/app-mocked --project=chromium
```

GitHub Actions runs both autonomous E2E layers:

- `e2e/harness` for shared table/layout mechanics
- `e2e/app-mocked` for the real application UI with mocked `/api`

## Docker build

Run from the monorepository root. The container serves Vue 3 at `/` and proxies `/api` to `HREASY_API_HOST`.

```shell
docker build -t hreasyweb:test web
docker run --rm -e HREASY_API_HOST=host.docker.internal:8081 -p8080:80 --name hreasyweb hreasyweb:test
```


## Legacy vue2 version

If for some reason you need to use legacy vue2 version, you can run it locally:
```bash
docker run --rm -it --name hreasyweb-old -e HREASY_API_HOST=host.docker.internal:8081  -p 8080:80 docker.io/abondin/hreasyweb:1.3.1
```

Where `host.docker.internal` is the address of the host machine.

After that you can access it at `http://localhost:8080/old`. (important: to add /old in URL)
