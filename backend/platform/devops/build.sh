#!/usr/bin/env bash

COLOR_OFF='\033[0m'

function out {
  COLOR='\033[0;35m'

  echo -e "${COLOR}[CI]: $1${COLOR_OFF}"
}

set -e

DEVOPS_FOLDER=$(cd `dirname $0` && pwd)

cd ${DEVOPS_FOLDER}/../

# TODO Add docker registry parameters
MVN_PARAMS=''
mvnp() {
    mvn ${MVN_PARAMS} "$@"
}
export -f mvnp


function getArtifactFinalName {
    mvnp org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.build.finalName 2>/dev/null | grep -Ev '(^\[|Download\w+:)'
}

function getDockerArtifactName {
    echo "hreasyplatform";
}

out "----------------------------------------------------"
out "Platform build:"
out ">> DOCKER_REPOSITORY= ${DOCKER_REPOSITORY}"
out ">> CI_DEPLOY_TAG= ${CI_DEPLOY_TAG}"

out "Maven build"

mvnp install -U


if  [ -z "$DOCKER_REPOSITORY" ]
    then
      DOCKER_IMAGE=`getDockerArtifactName`
    else
      DOCKER_IMAGE=${DOCKER_REPOSITORY}/`getDockerArtifactName`
fi
DOCKER_JOB_IMAGE_TAG=$DOCKER_IMAGE':'$CI_DEPLOY_TAG

out "Build Docker Image $DOCKER_JOB_IMAGE_TAG"

mvnp jib:dockerBuild -Dimage=$DOCKER_JOB_IMAGE_TAG
echo "------------- ${DOCKER_JOB_IMAGE_TAG} has been created"
