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
PLATFORM_HOME=${DEVOPS_ROOT_FOLDER}/../platform
WEB_HOME=${DEVOPS_ROOT_FOLDER}/../web

out "HR Easy full build"
out ">> DOCKER_REPOSITORY= ${DOCKER_REPOSITORY}"
out ">> CI_DEPLOY_TAG= ${CI_DEPLOY_TAG}"
out ">> PLATFORM_HOME=  ${PLATFORM_HOME}"
out ">> WEB_HOME= ${WEB_HOME}"

${PLATFORM_HOME}/devops/build.sh

${WEB_HOME}/devops/build.sh
