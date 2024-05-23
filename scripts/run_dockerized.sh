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

REACT_APP_UNAUTHED_LOGIN=true ./gradlew :api-$1:jibDockerBuild -Djib.to.image=api-$1:ci

docker run -d -p $targetport:8080 --net=host api-$1:ci
