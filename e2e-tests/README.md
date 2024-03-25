## UI automation tests with Playwright

### Set up
* Install dependencies and web browsers in **/e2e-tests** dir.
  >
  >  * cd e2e-tests/
  >  * npm install
  >  * npx playwright install --with-deps
  >

### Run tests
* Show all tests in all projects:
  > npx playwright test --list

* Run all tests in all projects:
  > npx playwright test

* Run tests in the one specific project

  Example: OurHealth-Chrome
  > npx playwright test --project="OurHealth-Chrome"

* Run **@ourhealth** tagged tests
  > npx playwright test --grep @ourhealth

* Read environment variables from a `.env.*` file to specify non-default environment variables.
  
  Example: reading `.env.local` file and run a specific test (pass the TEST_ENV and test name as arguments)
  > TEST_ENV=local npx playwright test src/tests/mailing-list.test.ts

### Test report
* Open the last test run report:
  > npx playwright show-report

* Test trace viewer: https://trace.playwright.dev/


### placeholder

