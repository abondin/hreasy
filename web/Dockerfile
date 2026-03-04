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
COPY legacy/vue2/package*.json legacy/vue2/
RUN npm ci
RUN npm ci --prefix legacy/vue2

# ---- Sources ----
COPY . .

# ---- Build Vue 2 app ----
RUN npm run build --prefix legacy/vue2

# ---- Build Vue 3 app ----
WORKDIR /root/frontend
ENV VITE_APP_BASE_PATH=/new/
RUN npm run build

# ---- Release ----
FROM nginx:alpine AS release
COPY --from=base /root/frontend/legacy/vue2/dist/ /opt/frontend/static-ui/
COPY --from=base /root/frontend/dist/ /opt/frontend/static-ui/new/
COPY --chmod=644 devops/full_proxy.conf /etc/nginx/conf.d/full_proxy.conf.template
COPY --chmod=755 devops/run.sh /run.sh

EXPOSE 80
ENTRYPOINT ["/bin/ash", "/run.sh"]
