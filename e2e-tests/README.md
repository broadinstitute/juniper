## UI automation tests with Playwright

### Set up
* Install dependencies and web browsers in **/e2e-tests** dir.
  >
  >  * cd e2e-tests/
  >  * npm install
  >  * npx playwright install --with-deps
  >

### Run tests
* Run all tests against local running development environment:
  >`npx playwright test`

* Read environment variables from a `.env` file against any environment.</p>*Example*: reading `.env.local` file and run one test
  > TEST_ENV=local npx playwright test src/tests/mailing-list.test.ts

### Test report
* Open the last test run report:
  > npx playwright show-report

* Test trace viewer: https://trace.playwright.dev/


### Run tests
* Run OurHealth UI visual tests
  > `npx `

