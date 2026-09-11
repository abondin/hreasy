FROM node:26-trixie-slim AS base

WORKDIR /root/frontend

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:alpine AS release
COPY --from=base /root/frontend/dist/ /opt/frontend/static-ui/
COPY --chmod=644 devops/full_proxy.conf /etc/nginx/conf.d/full_proxy.conf.template
COPY --chmod=755 devops/run.sh /run.sh

EXPOSE 80
ENTRYPOINT ["/bin/ash", "/run.sh"]
