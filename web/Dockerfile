# Базовый образ
# ---- Base Node ----
FROM node:lts-alpine AS base
RUN apk add --no-cache tini && mkdir -p /root/frontend

WORKDIR /root/frontend

# Set tini as entrypoint
ENTRYPOINT ["/sbin/tini", "--"]

# Full Build
# ---- Dependencies ----
COPY package*.json ./

# install node packages
RUN npm install

FROM base AS build-frontend-all

COPY . .

RUN npm run build

# ---- Release ----
from nginx AS release
COPY --from=build-frontend-all /root/frontend/dist/ /opt/frontend/static-ui/

ADD devops/full_proxy.conf /etc/nginx/conf.d/full_proxy.conf.template

ADD devops/run.sh /run.sh
CMD bash /run.sh
