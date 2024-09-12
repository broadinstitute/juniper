# Compliance Tools

Here is where we maintain our automation tooling for compliance.

## Synchronizing scope with Vanta
Our vanta integrations pull in *all* users from Broad, despite various scope limitations that we have
set in vanta.  To reset scope, run the SyncVantaUsers app, giving it the appropriate GOOGLE_CLOUD_PROJECT
and VANTA_CONFIG_SECRET values that point at a given secret within a given google cloud project.

This app can take 10m or more to run, since vanta has rate limits that we frequently hit, each requiring
a 60 second backoff time.  One particularly slow step is our workday integration.  It appears that workday
contains all Broadies, regardless of current employment, so filtering through this entire collection in
blocks of 100 users just takes a while.

When the integration completes, a summary is reported out to slack using the token and channel
indicated in VANTA_CONFIG_SECRET.

## Building and deploying
```
# build the jar file, putting an executable jar file into build/uber-jar (from repo root)
./gradlew compliance:jar

# deploy as GCP cloud function
gcloud --project=[GCP project] functions deploy --gen2 sync-vanta-scope \
   --entry-point bio.terra.pearl.compliance.SyncVantaUsers \
   --set-env-vars 'GOOGLE_CLOUD_PROJECT=[GCP project],VANTA_CONFIG_SECRET=[name of secret in secret manager]' \ 
   --runtime java21 --trigger-topic [pubsub topic] --source build/uber-jar --memory 512MB \ 
   --region [GCP region] --ingress-settings internal-only --timeout=60m
   
   ```

## Scheduling

This job runs as a pubsub-triggered google cloud function.  Set the schedule using GCP cloud scheduler and
have it post a message to the appropriate topic.