#!/bin/bash

APP_NAME=$1
DEV_EMAIL=$2

# Define the environment variables for each service
# Format is VAR_NAME:SOURCE:VALUE
# SOURCE can be static, vault, or gcpsm (although gcpsm is not yet supported)
# VALUE should be the value of the variable or the command to execute to get the value
# One day, maybe break this out into something more readable like JSON but for now this works.
ADMIN_API_ENV_VARS=(
  "REDIRECT_ALL_EMAILS_TO:static:$DEV_EMAIL"
  "B2C_TENANT_NAME:static:ddpdevb2c"
  "B2C_CLIENT_ID:vault:vault read -field value secret/dsp/ddp/b2c/dev/application_id"
  "B2C_POLICY_NAME:static:B2C_1A_ddp_admin_signup_signin_dev"
  "DSM_JWT_SIGNING_SECRET:vault:vault read -field jwt_signing_secret secret/dsp/ddp/d2p/dev/dsm"
  "SENDGRID_API_KEY:vault:vault read -field=api_key secret/dsp/ddp/d2p/dev/sendgrid"
  "TDR_SA_CREDS:vault:vault read -field=sa-key.json.b64 secret/dsp/ddp/d2p/dev/d2p-tdr-sa"
  "TDR_EXPORT_ENABLED:static:false"
  "TDR_EXPORT_STORAGE_ACCOUNT_NAME:vault:vault read -field=storage_account_name secret/dsp/ddp/d2p/dev/tdr-export-storage-account"
  "TDR_EXPORT_STORAGE_ACCOUNT_KEY:vault:vault read -field=storage_account_key secret/dsp/ddp/d2p/dev/tdr-export-storage-account"
  "TDR_EXPORT_STORAGE_CONTAINER_NAME:static:juniper-dataset-ingest"
  "DEPLOYMENT_ZONE:static:local"
  "AIRTABLE_AUTH_TOKEN:vault:vault read -field=authToken secret/dsp/ddp/d2p/dev/airtable"
)

PARTICIPANT_API_ENV_VARS=(
  "REDIRECT_ALL_EMAILS_TO:static:$DEV_EMAIL"
  "B2C_CONFIG_FILE:static:b2c-config.yml"
  "DSM_JWT_SIGNING_SECRET:vault:vault read -field jwt_signing_secret secret/dsp/ddp/d2p/dev/dsm"
  "SENDGRID_API_KEY:vault:vault read -field=api_key secret/dsp/ddp/d2p/dev/sendgrid"
  "SWAGGER_ENABLED:static:true"
)

# Parameter validation
if [ "$APP_NAME" = "ApiAdminApp" ]; then
    ENV_VARS=("${ADMIN_API_ENV_VARS[@]}")
elif [ "$APP_NAME" = "ApiParticipantApp" ]; then
    ENV_VARS=("${PARTICIPANT_API_ENV_VARS[@]}")
else
    echo "Unsupported APP_NAME. Please specify either 'ApiAdminApp' or 'ApiParticipantApp'."
    exit 1
fi

if [ -z "$DEV_EMAIL" ]; then
    echo "DEV_EMAIL is required. Usage: $0 APP_NAME DEV_EMAIL"
    exit 1
fi

ENV_VARIABLES=()

fetch_vault_variable() {
    VAULT_PATH="$1"
    VAULT_VARIABLE_NAME="$2"

    # Execute the vault command specified in the variable entry
    VAULT_OUTPUT=$($VAULT_PATH)

    echo "$VAULT_VARIABLE_NAME=$VAULT_OUTPUT"
}

# TODO: Note that the DSM secret actually uses GCP Secret Manager. For now this script just pulls it from Vault,
#       but that could get out of sync with the actual secret. We should update this to use GCP Secret Manager.
fetch_secret_manager_variable() {
  echo "Not yet implemented."
}

# Gather the environment variables
for var_entry in "${ENV_VARS[@]}"; do
    IFS=':' read -ra parts <<< "$var_entry"
    var_name="${parts[0]}"
    var_type="${parts[1]}"
    var_val="${parts[2]}"

    # Check for the variable source
    if [ "$var_type" = "static" ]; then
        ENV_VARIABLES+=("$var_name=$var_val")
    elif [ "$var_type" = "vault" ]; then
        VAULT_VAR=$(fetch_vault_variable "$var_val" "$var_name")
        ENV_VARIABLES+=("$VAULT_VAR")
    elif [ "$var_type" = "gcpsm" ]; then
        SM_VAR=$(fetch_secret_manager_variable "$var_val" "$var_name")
        ENV_VARIABLES+=("$SM_VAR")
    else
        echo "Unsupported variable type for $var_name: $var_type"
    fi
done

IFS=";"
echo "${ENV_VARIABLES[*]}"
