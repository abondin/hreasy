#!/usr/bin/env bash

set -e

COLOR_OFF='\033[0m'

function out {
  COLOR='\033[0;35m'

  echo -e "${COLOR}[HREASY]: $1${COLOR_OFF}"
}

# TODO
export CI_DEPLOY_TAG=latest

cd ../

export HOME_FOLDER=`pwd`

DOCKER_REPOSITORY=
#docker.k8s-usn.stm.local:80

function build {
    out "Build in docker"

    if  [ -z "$DOCKER_REPOSITORY" ]
    then
      DOCKER_IMAGE=hreasyweb
    else
      DOCKER_IMAGE=${DOCKER_REPOSITORY}/hreasyweb
    fi
    DOCKER_JOB_IMAGE_TAG=${DOCKER_IMAGE}:${CI_DEPLOY_TAG}

    out "DOCKER_IMAGE=$DOCKER_IMAGE"
    out "DOCKER_JOB_IMAGE_TAG=$DOCKER_JOB_IMAGE_TAG"

    versionHash=`git rev-parse HEAD`
    versionBuild=$CI_DEPLOY_TAG
    buildDate=`date`

    # sed -i "s/%VERSION_HASH%/${versionHash}/g" config/prod.env.js
    # sed -i "s/%VERSION_BUILD%/${versionBuild}/g" config/prod.env.js
    # sed -i "s/%BUILD_TIME%/${buildDate}/g" config/prod.env.js

    echo "Tag pipeline job tag"
    docker build -t $DOCKER_JOB_IMAGE_TAG .
    #docker push $DOCKER_JOB_IMAGE_TAG

    echo "Label latest docker tag & push"
    docker tag $DOCKER_JOB_IMAGE_TAG $DOCKER_IMAGE:latest
    #docker push $DOCKER_IMAGE:latest
}

build

echo "Docker build tag ${CI_DEPLOY_TAG}"

export status=0

# below is only cleanup
# ignore errors - we successfully deployed dockers
set +e

exit $status
