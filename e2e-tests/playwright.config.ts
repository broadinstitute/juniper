import { defineConfig, devices } from '@playwright/test'
import { config } from 'dotenv'

const CI = !!process.env.CI
const { SLOW_MO, OURHEALTH_PARTICIPANT_URL } = process.env

/**
 * Pass in `TEST_ENV` environment variable to read environment variables from .env.* file. Example: TEST_ENV=local
 *  - override hardcoded URL in global-setup.ts
 *  - non-default environment variables
 *  - specify new environment variables
 */
const { TEST_ENV } = process.env
if (TEST_ENV) {
  config({ path: `.env.${TEST_ENV}`, override: true })
} else {
  config()
}

/**
 * See https://playwright.dev/docs/test-configuration.
 */
export default defineConfig({
  //testDir: './src/tests',
  testMatch: '**/*.test.ts',
  /* timeout for each test, 30 seconds by default. */
  timeout: 60 * 1000,
  /* Run tests in files in parallel */
  fullyParallel: true,
  /* Fail the build on CI if you accidentally left test.only in the source code. */
  forbidOnly: CI,
  /* Retry on CI only */
  retries: CI ? 2 : 0,
  /* Opt out of parallel tests on CI. */
  workers: CI ? 1 : undefined,
  /* Reporter to use. See https://playwright.dev/docs/test-reporters */
  reporter: [
    ['list'],
    ['html', {  open: 'never' }]
  ],
  /* Shared settings for all the projects below. See https://playwright.dev/docs/api/class-testoptions. */
  use: {
    /* Base URL to use in actions like `await page.goto('/')`. */
    // baseURL: 'http://127.0.0.1:3000',

    actionTimeout: 10 * 1000,

    /* Collect trace when retrying the failed test. See https://playwright.dev/docs/trace-viewer */
    trace: CI ? 'on-first-retry' : 'retain-on-failure',
    /* https://playwright.dev/docs/videos#record-video */
    video: CI ? 'on-first-retry' : 'on',

    launchOptions: {
      slowMo: SLOW_MO ? parseInt(SLOW_MO) : 100,
      // Account for minor difference in text rendering and resolution between headless and headed mode
      ignoreDefaultArgs: ['--hide-scrollbars']
    }
  },
  expect: {
    /* faster timeouts of local tests helps speed up creating new tests, at the cost of flakiness */
    timeout: CI ? 10000 : 5000,
    toHaveScreenshot: {
      // Account for minor difference in text rendering and resolution between headless and headed mode
      threshold: 1,
      maxDiffPixelRatio: 1
    }
  },

  globalSetup: require.resolve('./global-setup.ts'),

  /* Configure projects for major browsers */
  projects: [
    /* Running OurHealth study tests:
     * npx playwright test --project="OurHealth-Chrome"
     */
    {
      name: 'OurHealth-Chrome',
      testDir: './src/tests/studies/ourhealth',
      use: {
        ...devices['Desktop Chrome'],
        contextOptions: {
          ignoreHTTPSErrors: true
        },
        ignoreHTTPSErrors: true,
        // set OURHEALTH_PARTICIPANT_URL environment variable to override default URLs
        baseURL: OURHEALTH_PARTICIPANT_URL
          ? OURHEALTH_PARTICIPANT_URL
          : CI
            ? 'http://sandbox.ourhealth.localhost:8081'
            : 'https://sandbox.ourhealth.localhost:3001'
      }
    }

    // {
    //   name: 'OurHealth-iPhone14',
    //   testDir: './src/tests/studies/ourhealth',
    //   use: {
    //     ...devices['iPhone 14'],
    //     contextOptions: {
    //       ignoreHTTPSErrors: true
    //     },
    //     ignoreHTTPSErrors: true,
    //     // set OURHEALTH_PARTICIPANT_URL environment variable to override default URLs
    //     baseURL: OURHEALTH_PARTICIPANT_URL
    //       ? OURHEALTH_PARTICIPANT_URL
    //       : CI
    //         ? 'http://sandbox.ourhealth.localhost:8081'
    //         : 'https://sandbox.ourhealth.localhost:3001'
    //   }
    // }

    // {
    //   name: 'firefox',
    //   use: { ...devices['Desktop Firefox'] }
    // },

    // {
    //   name: 'webkit',
    //   use: { ...devices['Desktop Safari'] }
    // }

    /* Test against mobile viewports. */
    // {
    //   name: 'Mobile Chrome',
    //   use: { ...devices['Pixel 5'] }
    // },
    // {
    //   name: 'Mobile Safari',
    //   use: { ...devices['iPhone 12'] }
    // },

    /* Test against branded browsers. */
    // {
    //   name: 'Microsoft Edge',
    //   use: { ...devices['Desktop Edge'], channel: 'msedge' }
    // },
    // {
    //   name: 'Google Chrome',
    //   use: { ..devices['Desktop Chrome'], channel: 'chrome' }
    // },
  ],

  /* Run applications before starting the tests */
  webServer: [
    {
      command: 'cd .. && ./gradlew :api-admin:bootRun',
      url: 'http://localhost:8080/status',
      timeout: 180 * 1000,
      reuseExistingServer: !CI
    },
    {
      command: 'cd .. && ./gradlew :api-participant:bootRun',
      url: 'http://localhost:8081/status',
      timeout: 180 * 1000,
      reuseExistingServer: !CI
    }
    // In CI, test against the UI bundled into the Java app.
    // Otherwise, test against the UI dev server.
    /*
    ...(
      CI ? [] : [
        {
          command: 'cd .. && REACT_APP_UNAUTHED_LOGIN=true HTTPs=true npm -w ui-admin start',
          url: 'https://localhost:3000',
          timeout: 120 * 1000,
          reuseExistingServer: !CI,
          ignoreHTTPSErrors: true
        },
        {
          command: 'cd .. && REACT_APP_UNAUTHED_LOGIN=true HTTPS=true npm -w ui-participant start',
          url: 'https://localhost:3001',
          timeout: 120 * 1000,
          reuseExistingServer: !CI,
          ignoreHTTPSErrors: true
        }
      ]
    ) */
  ]
})
