import { defineConfig, devices } from '@playwright/test'
import path from 'path';

const { CI } = process.env

/**
 * Read environment variables from file.
 * https://github.com/motdotla/dotenv
 */
// require('dotenv').config();

/**
 * See https://playwright.dev/docs/test-configuration.
 */
export default defineConfig({
  //testDir: './src/tests',
  testMatch: '**/*.test.ts',
  /* Run tests in files in parallel */
  fullyParallel: true,
  /* Fail the build on CI if you accidentally left test.only in the source code. */
  forbidOnly: !!CI,
  /* Retry on CI only */
  retries: CI ? 2 : 0,
  /* Opt out of parallel tests on CI. */
  workers: CI ? 1 : undefined,
  /* Reporter to use. See https://playwright.dev/docs/test-reporters */
  reporter: 'html',
  /* Shared settings for all the projects below. See https://playwright.dev/docs/api/class-testoptions. */
  use: {
    /* Base URL to use in actions like `await page.goto('/')`. */
    // baseURL: 'http://127.0.0.1:3000',

    /* Collect trace when retrying the failed test. See https://playwright.dev/docs/trace-viewer */
    trace: 'on-first-retry',
    /* https://playwright.dev/docs/videos#record-video */
    video: 'on-first-retry'
  },
  expect: {
    /* faster timeouts of local tests helps speed up creating new tests, at the cost of flakiness */
    timeout: CI ? 10000 : 5000
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
    //   /* Running exclusively all admin tests on OurHealth study: npx playwright test --project="admin-ourhealth" */
    //   name: 'admin-ourhealth',
    //   testDir: './src/tests/admin/ourhealth',
    //   use: {
    //     ...devices['Desktop Chrome'],
    //     contextOptions: {
    //       ignoreHTTPSErrors: true
    //     },
    //     ignoreHTTPSErrors: true,
    //     baseURL: process.env.baseURL || CI ? 'http://localhost:8080' : 'https://localhost:3000'
    //   }
    // },
    //
    // /* Running exclusively all OurHealth study registration tests: npx playwright test --project="ourhealth" */
    // {
    //   name: 'ourhealth',
    //   testDir: './src/tests/studies/ourhealth',
    //   use: {
    //     ...devices['Desktop Chrome'],
    //     contextOptions: {
    //       ignoreHTTPSErrors: true
    //     },
    //     ignoreHTTPSErrors: true,
    //     baseURL: process.env.baseURL || CI ? 'http://sandbox.ourhealth.localhost:8081' : 'https://sandbox.ourhealth.localhost:3001'
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
    },
    // In CI, test against the UI bundled into the Java app.
    // Otherwise, test against the UI dev server.
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
    )
  ]
})
