#!/usr/bin/env bash

COLOR_OFF='\033[0m'

function out {
  COLOR='\033[0;35m'

  echo -e "${COLOR}[CI]: $1${COLOR_OFF}"
}

set -e

if  [ -z "$CI_DEPLOY_TAG" ]
    then
      out "CI_DEPLOY_TAG not set. Using default one"
      export CI_DEPLOY_TAG=latest
fi
if  [ -z "$DOCKER_REPOSITORY" ]
    then
      out "DOCKER_REPOSITORY not set. Using STM one"
      export DOCKER_REPOSITORY=docker.k8s-usn.stm.local:80
fi

DEVOPS_ROOT_FOLDER=$(cd `dirname $0` && pwd)
BACKEND_HOME=${DEVOPS_ROOT_FOLDER}/../backend
WEB_HOME=${DEVOPS_ROOT_FOLDER}/../web

out "HR Easy full build"
out ">> DOCKER_REPOSITORY= ${DOCKER_REPOSITORY}"
out ">> CI_DEPLOY_TAG= ${CI_DEPLOY_TAG}"
out ">> BACKEND_HOME=  ${BACKEND_HOME}"
out ">> WEB_HOME= ${WEB_HOME}"

function docker_image {
  echo "${DOCKER_REPOSITORY}/$1:${CI_DEPLOY_TAG}"
}

out "Maven backend build"
mvn -f "${BACKEND_HOME}/pom.xml" install -U

out "Build Docker Image $(docker_image hreasyplatform)"
mvn -f "${BACKEND_HOME}/platform/pom.xml" jib:dockerBuild -Dimage="$(docker_image hreasyplatform)"

out "Build Docker Image $(docker_image hreasynotifyms)"
mvn -f "${BACKEND_HOME}/notify-ms/pom.xml" jib:dockerBuild -Dimage="$(docker_image hreasynotifyms)"

out "Build Docker Image $(docker_image hreasytelegram)"
mvn -f "${BACKEND_HOME}/telegram/pom.xml" jib:dockerBuild -Dimage="$(docker_image hreasytelegram)"

out "Build Docker Image $(docker_image hreasyweb)"
docker build -t "$(docker_image hreasyweb)" "${WEB_HOME}"
