#!/bin/sh

# builds and runs the specified docker image
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 [participant|admin]"
    exit 1
fi

if [ "$1" != "participant" ] && [ "$1" != "admin" ]; then
    echo "Usage: $0 [participant|admin]"
    exit 1
fi

targetport="8080"
if [ "$1" != "participant" ]; then
    targetport="8081"
fi

APP_NAME=api-$1
IMAGE_NAME=$APP_NAME:ci

REACT_APP_UNAUTHED_LOGIN=true ./gradlew $APP_NAME:jibDockerBuild -Djib.to.image=$IMAGE_NAME

docker run -d -p $targetport:8080 --net=host $IMAGE_NAME
