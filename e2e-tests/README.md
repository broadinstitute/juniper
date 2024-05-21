## UI automation tests with Playwright

### Set up
* Install dependencies and Playwright web browsers in **/e2e-tests** dir.
```shell
npm installn && npx playwright install --with-deps
```


### Build and Lint
* From **/e2e-tests** dir
```shell
npm run build && npm run lint
```

### Run tests
* Show all tests in all projects:
  ```shell
  npx playwright test --list
  ```

* Run all tests in all projects:
  ```shell
  npx playwright test
  ```

* Run tests in the one specific project

  Example: only `OurHealth-Chrome`
  ```shell
  npx playwright test --project="OurHealth-Chrome"
  ```

* Run **@ourhealth** tagged tests
  ```shell
  npx playwright test --grep @ourhealth
  ```


#### Environment variables
* Specify an optional `.env.*` file for the tests to read environment variables.

  - override hardcoded URL in **global-setup.ts**
  - override default environment variables
  - specify new environment variables
   
Example: 
Reading a file named `.env.local` and run a specific test (pass the TEST_ENV and test name as arguments)
  ```shell
  TEST_ENV=local npx playwright test src/tests/studies/ourhealth/mailing-list.test.ts
  ```


### Test report
* Open the last test run report:
  > npx playwright show-report

* Test trace viewer: https://trace.playwright.dev/


### placeholder

### Test [wiki](https://github.com/broadinstitute/juniper/wiki/E2E-Tests)
