#!/bin/sh
SERVER_NAME="localhost:8080"
set -x

curl -X POST "$SERVER_NAME/api/internal/v1/populate/setup?filePathName=seed"
