#!/bin/sh
SERVER_NAME="localhost:8080"
set -u

ACCESS_TOKEN=$(az account get-access-token | jq -r .accessToken)
curl -X POST -H "Authorization: Bearer $ACCESS_TOKEN" "$SERVER_NAME/api/internal/v1/populate/base_seed"
