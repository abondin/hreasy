# Базовый образ
# ---- Base Node ----
FROM node:20-bullseye-slim AS base
RUN apt-get update \
 && apt-get install -y --no-install-recommends tini \
 && rm -rf /var/lib/apt/lists/* \
 && mkdir -p /root/frontend

WORKDIR /root/frontend

# Set tini as entrypoint
ENTRYPOINT ["/usr/bin/tini", "--"]

# ---- Dependencies ----
COPY package*.json ./
COPY migration/vue3-skeleton/package*.json migration/vue3-skeleton/
RUN npm ci
RUN npm ci --prefix migration/vue3-skeleton

# ---- Sources ----
COPY . .

# ---- Build Vue 2 app ----
RUN npm run build

# ---- Build Vue 3 app ----
WORKDIR /root/frontend/migration/vue3-skeleton
ENV VITE_APP_BASE_PATH=/app-v3/
RUN npm run build
WORKDIR /root/frontend

# ---- Release ----
FROM nginx:alpine AS release
COPY --from=base /root/frontend/dist/ /opt/frontend/static-ui/
COPY --from=base /root/frontend/migration/vue3-skeleton/dist/ /opt/frontend/static-ui/app-v3/
COPY --chmod=644 devops/full_proxy.conf /etc/nginx/conf.d/full_proxy.conf.template
COPY --chmod=755 devops/run.sh /run.sh

EXPOSE 80
ENTRYPOINT ["/bin/ash", "/run.sh"]
