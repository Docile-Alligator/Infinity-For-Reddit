#!/bin/bash

CONTAINER_NAME="infinity-builder"

source .env || true

docker build -t infinity-builder .
docker run -it -e CUSTOM_API_KEY=$CUSTOM_API_KEY -e REDDIT_USER=$REDDIT_USER --name $CONTAINER_NAME infinity-builder /bin/bash
docker cp $CONTAINER_NAME:/content/app/build/outputs/apk/release/app-release.apk ./Infinity.apk
docker rm $CONTAINER_NAME
