# Juniper

## Overview
Arbor is intended to allow the collection and management of 3 distinct data types in the context of a research study: survey, genomic, and medical records.
Pre-registration screeners, registration, consent forms, surveys, genomic kit testing, and medical records retrieval can all be configured from inside the admin interface.
the participant-facing portal then displays a home page and the configured content to study participants

### Structure

#### Core
Contains base POJO data models, DAOs, and services.  The core is responsible for postgres data persistence,
and so this is also where liquibase lives.  Running the CoreCliApp will do nothing except run liquibase

#### Populate
Contains functionality for populating entities from files, as well as basic seed data.  this includes populate
DTOs, and populate services.  the PopulateCliApp can be used to populate specific entities from files via command-line

#### Study Manager Tool

##### Study manager API
   The Admin API server serves requests from the Admin UI. It is built on top of the services in both core and populate.
   In development, it serves at localhost:8080.  Lives in `api-admin` directory
##### Study Manager UI
   Study manager UI is a create-react-app SPA.  Lives in `ui-admin` directory.  Serves on localhost:3000 in development

#### Participant
##### Participant API
   The Participant API server serves requests from the Participant UI. It is built on top of the services in both core and populate.
   In development, it serves at localhost:8081.  Lives in `api-participant` directory
##### Study Manager UI
   Participant UI is a create-react-app SPA.  Lives in `ui-participant` directory.  Serves on localhost:3001 in development
   


## Local development

### Prerequisites
* Java 17
* IntelliJ
* Node v16+
* Docker

### Setup

#### Database setup
run `./local-dev/run_postgres.sh start`
This will start a postgres container with a schema and user configured

#### IDE Setup
Open the root folder in IntelliJ.  

* **server:**

   * Make sure IntelliJ is set to Java 17 in *two* places

      * Project Structure > Project Settings > Project > SDK
      * Preferences > Build, Execution, Deployment > Build Tools > Gradle > Gradle Projects > \[this project\] > Gradle JVM
         * Recommended setting for this is "Project SDK"
   * In Preferences > Build, Execution, Deployment > Compiler > Annotation Processors, make sure annotation processing is enabled (otherwise lombok getters/setters won't work)
   * Create two Spring Boot Run/Debug Configurations.
     * ApiAdminApp (in api-admin module)
       * set active profiles of "human-readable-logging" and "development"
       * disable launch optimization
       * set environment variable: `REDIRECT_ALL_EMAILS_TO=<<your email address>>`
       * set environment variable: `TDR_SA_CREDS=<<vault read -field=sa-key.json.b64 secret/dsp/ddp/d2p/dev/d2p-tdr-sa>>`
     * ApiParticipantApp (in api-participant module)
        * set active profiles of "human-readable-logging" and "development"
        * set environment variable: `SENDGRID_API_KEY=<<vault read -field=api_key secret/dsp/ddp/d2p/dev/sendgrid>>`
        * set environment variable: `REDIRECT_ALL_EMAILS_TO=<<your email address>>`
        * set environment variable: `B2C_TENANT_NAME=ddpdevb2c`
        * set environment variable: `B2C_CLIENT_ID=<<vault read -field value secret/dsp/ddp/b2c/dev/application_id>>`
        * set environment variable: `B2C_POLICY_NAME=B2C_1A_ddp_participant_signup_signin_dev`
        * set environment variable: `B2C_CHANGE_PASSWORD_POLICY_NAME=B2C_1A_ddp_participant_change_password_dev`
        * disable launch optimization
        
         
### Running the application
#### Admin tool (study manager, population)
* API (api-admin module)
In intelliJ, you can either run ApiAdminApp (from the api-admin module) directly, or execute the "bootRun" gradle task.
In basic development mode, this will only serve the API, not the frontend assets.

To make the application useful, you will want to populate some users and studies.  After the admin API is running, 
from the root project directory, run
```
./scripts/populate_portal.sh ourhealth
```

##### Admin UI (ui-admin module)
From the command line:
  ```
  npm install
  npm -w ui-core run build
  REACT_APP_B2C_TENANT_NAME=ddpdevb2c REACT_APP_B2C_CLIENT_ID=$(vault read -field value secret/dsp/ddp/b2c/dev/application_id) npm -w ui-admin start
  ```
(note that you can just run `npm -w ui-admin start` if you don't need to test B2C login functionality)
Then go to `localhost:3000` 

##### Participant API (api-participant module)
In intelliJ, you can either run ApiParticipantApp (from the api-participant module) directly, or execute the "bootRun" gradle task.
In basic development mode, this will only serve the API, not the frontend assets.

##### Participant UI (ui-participant module)
* Participant UI:  From the command line:
  ```
  npm install
  npm -w ui-core run build
  HTTPS=true npm -w ui-participant start
  ```
If you encounter an error like "No package 'pixman-1' found" on npm install, you may need additional dependencies.
If on OS X, try running `brew install pkg-config cairo pango libpng jpeg giflib librsvg pixman`
(note that you can just run `npm -w ui-participant start` if you don't need to test B2C login functionality)
Then go to `sandbox.ourhealth.localhost:3001`
(Notice how you need the environment name and portal name as subdomains)


### Feature Development 

#### Adding a new model 
1. Create the schema, models, and services
   1. Create your POJO model in `core/src/main/java/bio/terra/pearl/core/model`, you will almost certainly want to extend
   `BaseEntity`
   2. Add a liquibase changeset in `core/src/main/resources/db/changelog/changesets` to add/update any DB schema
   3. Add a DAO in `core/src/main/java/bio/terra/pearl/core/dao`.  Extending `BaseJdbiDao` will give you built-in create,
   find, and delete methods.  BaseJDBIDao makes some basic assumptions about which fields in your POJO should be persisted, and
   queried, so you may have to override those as-needed if your POJO is complex.
   4. Add a test for your DAO in `core/src/test/java/bio/terra/pearl/core/dao`  Assuming you just overrode BaseJdbiDao,
   a simple create/get/delete test should be sufficient, and will mainly serve to confirm that the database schema is correct
   5. Add a service in `core/src/test/java/bio/terra/pearl/core/service` for exposing any DAO methods, and handling
   transaction wrappers, and implementing any complex logic
   6. Add a test for any non-trivial service methods in `core/src/test/java/bio/terra/pearl/core/service`
2. Add synthetic data for your new model
   1. Add seed data in `populate/src/main/resources/seed`.  This may be either adding new files, or updating
   existing files with new objects
   2. Create a populator (or update an existing populator) in `populate/src/main/java/bio/terra/pearl/populate/service`
   In general, a separate populator should be created if the model is a standalone entity.  If it is wholly owned
   by another object, the populate logic should either be in the populator for that entity, or in the service
   3. Use the PopulateCliApp to test that the populator puts your new seed data in the database
3. Add a controller
   1. Edit `api-admin/src/main/resources/api/openapi.yml` to add a new path and endpoint configuration
   for your controller
   2. Add a controller in `api-admin/src/main/java/bio/terra/pearl/api/admin/controller` implementing the
   interface generated by the openapi yaml.
   3. visit localhost:8080 to confirm your new endpoint works.

#### Adding environment variables
  Helm charts:
  https://github.com/broadinstitute/terra-helmfile/tree/master/values/app/d2p/live
