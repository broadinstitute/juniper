#!/bin/sh
# populates a survey in development environment
# populate_survey.sh [portalShortcode] [filePath from populate/src/main/resources/seed]
# e.g. populate_survey.sh ourhealth portals/ourhealth/studies/ourheart/surveys/preEnroll.json
SERVER_NAME="localhost:8080"
set -x

curl -X POST "$SERVER_NAME/api/internal/v1/populate/survey/$1?filePathName=$2"

echo ""
