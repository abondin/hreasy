#!/usr/bin/env bash

COLOR_OFF='\033[0m'

function out {
  COLOR='\033[0;35m'

  echo -e "${COLOR}[CI]: $1${COLOR_OFF}"
}

set -e

cd ../

HOME_FOLDER=`pwd`

# TODO Add docker registry parameters
MVN_PARAMS=''
mvnp() {
    mvn ${MVN_PARAMS} "$@"
}
export -f mvnp


function getArtifactFinalName {
    mvnp org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.build.finalName 2>/dev/null | grep -Ev '(^\[|Download\w+:)'
}
function getDockerRepository {
    echo "";
}
function getDockerArtifactName {
    echo "hreasyplatform";
}


echo "Maven build"

mvnp install -U


echo "Build Docker Image"
DOCKER_IMAGE=`getDockerRepository``getDockerArtifactName`
DOCKER_JOB_IMAGE_TAG=$DOCKER_IMAGE':latest'
docker build -t $DOCKER_JOB_IMAGE_TAG --build-arg JAR_FILE=target/`getArtifactFinalName`-exec.jar .
echo "------------- ${DOCKER_JOB_IMAGE_TAG} has been created"
