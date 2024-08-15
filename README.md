
# Juniper

## Overview
Juniper is intended to allow the collection and management of 3 distinct data types in the context of a research study: survey, genomic, and medical records.
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
* Java 21
* IntelliJ
* Node v20+ (v20.10.0 tested)
* Docker
* Homebrew 4+

### Setup

#### Database setup
Run `./local-dev/run_postgres.sh start`
This will start a Postgres container with a schema and database user configured. This Postgres container is utilized by both the
local development environment and the unit/integration tests.

#### IDE Setup
In IntelliJ, File -> New -> Project from Existing Sources.  When importing the project, make sure it's set as a gradle project  
![image](https://github.com/broadinstitute/juniper/assets/2800795/dd2cf363-f761-47bc-9620-28e47a20feff)


* **Server:**

   * Make sure IntelliJ is set to Java 21 in *two* places
      * Project Structure > Project Settings > Project > SDK
      * Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle Projects > \[this project\] > Gradle JVM
         * Recommended setting for this is "Project SDK"
   * If using `nvm` to manage Node versions, make sure the correct version is set in Intellij to match your terminal 
     * Set the default Node used by the editor at Settings > Languages & Frameworks > Typescript > Node Interpreter 
     * If you wish to run tests within the IDE, you will also need to edit the Node used by the Jest configuration
       * Run > Edit Configurations... > Edit Configuration templates (bottom left) > Jest > Node Interpreter
   * Enable auto linting on save for Javascript files
     * Settings > Languages & Frameworks > JavaScript > Code Quality Tools > ESLint
       * Select `Automatic ESLint Configuration`
       * Select `Run eslint --fix on save`
   * In Settings > Build, Execution, Deployment > Compiler > Annotation Processors, make sure annotation processing is enabled (otherwise lombok getters/setters won't work)
   * Create two Spring Boot Run/Debug Configurations (Run > Edit Configurations > + > Spring Boot). These might already exist when you clone the repository.
     * ApiAdminApp (in api-admin module)
       * Set the Active profiles field to: `human-readable-logging, development`
       * Disable launch optimization by clicking `Modify options > Disable launch optimization`
       * Render environment variables with `bash ./local-dev/render_environment_vars.sh ApiAdminApp <YOUR_EMAIL>`. 
         * The output should be a semicolon-separated list of environment variables.
         * Copy this output into the "Environment variables" field of the run configuration. (Click `Modify options > Environment variables` if this is not visible)
       * Your final Run Configuration should look similar to this:
        * <img width="840" alt="Admin API run configuration" src="https://github.com/broadinstitute/juniper/assets/7257391/ae82332c-628f-40e1-8917-b7536af1eb02">
     * ApiParticipantApp (in api-participant module)
        * Set the Active profiles field to: `human-readable-logging, development`
        * Disable launch optimization by clicking `Modify options > Disable launch optimization`
        * Render environment variables with `bash ./local-dev/render_environment_vars.sh ApiParticipantApp <YOUR_EMAIL>`
          * The output should be a semicolon-separated list of environment variables.
          * Copy this output into the "Environment variables" field of the run configuration. (Click `Modify options > Environment variables` if this is not visible)
        * Your final Run Configuration should look similar to this:
        * <img width="840" alt="Participant API run configuration" src="https://github.com/broadinstitute/juniper/assets/7257391/b8e006fe-65a7-4731-9ab1-bda8d38b480b">



### Running the application
#### Admin tool (study manager, population)
##### Admin API (api-admin module)
In IntelliJ, you can either run ApiAdminApp directly (use the Run menu to choose ApiAdminApp), or execute the "bootRun" gradle task.
In basic development mode, this will only serve the API, not the frontend assets.

To make the application useful, you will want to populate some users and studies.  After the admin API is running, 
from the root project directory, run
```
./scripts/populate_portal.sh ourhealth
```

##### Admin UI (ui-admin module)

On OSX, you will need to install prerequisites:

  ```
  brew install pkg-config cairo pango libpng jpeg giflib librsvg pixman
  npm install
  ```

Then, you are ready to run the UI:
  ```
  npm -w ui-core run build
  HTTPS=true npm -w ui-admin start
  ```
Then go to `localhost:3000`
you can log in as "dbush@broadinstitute.org" using developer mode login to start.  Then use the manage users UI to add yourself,
or add yourself to the initial seed data in `populate/src/main/resources/seed/adminUsers` and the BaseSeedPopulator.ADMIN_USERS_TO_POPULATE.
Once you do that, your user will be created in the database on every clean if the database is empty.

#### Participant tool

##### Participant API (api-participant module)
In IntelliJ, you can either run ApiParticipantApp directly (use the Run menu to choose ApiParticipantApp), or execute the "bootRun" gradle task.
In basic development mode, this will only serve the API, not the frontend assets.

##### Participant UI (ui-participant module)

Similar to the Admin UI, you will need to first install prerequisites if not done so already:

  ```
  brew install pkg-config cairo pango libpng jpeg giflib librsvg pixman
  npm install
  ```

Then, you are ready to run the UI:

  ```
  npm -w ui-core run build
  HTTPS=true npm -w ui-participant start
  ```

(note that you can just run `VITE_UNAUTHED_LOGIN=true HTTPS=true npm -w ui-participant start` if you don't need to test B2C login functionality)
Then go to `sandbox.ourhealth.localhost:3001`
(Notice how you need the environment name and portal name as subdomains)  There are a number of pre-existing participants you 
can log in as, such as jsalk@test.com or consented@test.com.  All synthetic users have the password 'd2p4eva!'

### Unit tests

#### Server:
* To run all backend unit tests, you can run `./gradlew test` from the command line.
* You can also run individual tests or test suites from IntelliJ by right-clicking on the test or test suite and selecting "Run".

#### UI:
* You can run all frontend tests by running `npm --workspace=ui-core --workspace=ui-admin --workspace=ui-participant test` from the command line.
* You can also run individual tests or test suites from IntelliJ by right-clicking on the test or test suite and selecting "Run".

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
