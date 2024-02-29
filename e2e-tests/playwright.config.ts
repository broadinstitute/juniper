import { defineConfig, devices } from '@playwright/test'

/**
 * Read environment variables from file.
 * https://github.com/motdotla/dotenv
 */
// require('dotenv').config();

/**
 * See https://playwright.dev/docs/test-configuration.
 */
export default defineConfig({
  testDir: './tests',
  /* Run tests in files in parallel */
  fullyParallel: true,
  /* Fail the build on CI if you accidentally left test.only in the source code. */
  forbidOnly: !!process.env.CI,
  /* Retry on CI only */
  retries: process.env.CI ? 2 : 0,
  /* Opt out of parallel tests on CI. */
  workers: process.env.CI ? 1 : undefined,
  /* Reporter to use. See https://playwright.dev/docs/test-reporters */
  reporter: 'html',
  /* Shared settings for all the projects below. See https://playwright.dev/docs/api/class-testoptions. */
  use: {
    /* Base URL to use in actions like `await page.goto('/')`. */
    // baseURL: 'http://127.0.0.1:3000',

    /* Collect trace when retrying the failed test. See https://playwright.dev/docs/trace-viewer */
    trace: 'on-first-retry'
  },

  globalSetup: require.resolve('./global-setup.ts'),

  /* Configure projects for major browsers */
  projects: [
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        contextOptions: {
          ignoreHTTPSErrors: true
        },
        ignoreHTTPSErrors: true
      }
    }

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
      reuseExistingServer: !process.env.CI
    },
    {
      command: 'cd .. && ./gradlew :api-participant:bootRun',
      url: 'http://localhost:8081/status',
      timeout: 180 * 1000,
      reuseExistingServer: !process.env.CI
    },
    // In CI, test against the UI bundled into the Java app.
    // Otherwise, test against the UI dev server.
    ...(
      process.env.CI ? [] : [
        {
          command: 'cd .. && REACT_APP_UNAUTHED_LOGIN=true HTTPs=true npm -w ui-admin start',
          url: 'https://localhost:3000',
          timeout: 120 * 1000,
          reuseExistingServer: !process.env.CI,
          ignoreHTTPSErrors: true
        },
        {
          command: 'cd .. && REACT_APP_UNAUTHED_LOGIN=true HTTPS=true npm -w ui-participant start',
          url: 'https://localhost:3001',
          timeout: 120 * 1000,
          reuseExistingServer: !process.env.CI,
          ignoreHTTPSErrors: true
        }
      ]
    )
  ]
})
