#!/bin/sh
# populates a survey in development environment
# populate_survey.sh [portalShortcode] [filePath from populate/src/main/resources/seed]
# e.g. populate_survey.sh ourhealth portals/ourhealth/studies/ourheart/surveys/preEnroll.json
SERVER_NAME="localhost:8080"
set -u

ACCESS_TOKEN=$(az account get-access-token | jq -r .accessToken)
curl -X POST -H "Authorization: Bearer $ACCESS_TOKEN" "$SERVER_NAME/api/internal/v1/populate/survey/$1?filePathName=$2"

echo ""
