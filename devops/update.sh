#!/usr/bin/bash

docker pull docker.io/abondin/hreasyplatform:latest
docker pull docker.io/abondin/hreasyweb:latest
docker pull docker.io/abondin/hreasytelegram:latest
/usr/local/bin/docker-compose up -d --no-deps --force-recreate --build hreasyplatform hreasyweb hreasytg
