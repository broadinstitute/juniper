import { exec } from 'child_process'

const runPopulatePortalScript = (): Promise<undefined> => {
  return new Promise((resolve, reject) => {
    exec('../scripts/populate/populate_portal.sh demo ourhealth', (err, stdout, stderr) => {
      if (err) {
        console.error(err.message)
        console.error(stderr)
        reject(err)
      } else {
        resolve(undefined)
      }
    })
  })
}

/**
 * Setup to run before all tests.
 * https://playwright.dev/docs/test-global-setup-teardown
 */
const globalSetup = async () => {
  // In CI, test against the UI bundled into the Java app.
  // Otherwise, test against the UI dev server.
  if (process.env.CI) {
    process.env.ADMIN_URL = 'http://localhost:8080'
    process.env.PARTICIPANT_URL = 'http://sandbox.demo.localhost:8081'
  } else {
    process.env.ADMIN_URL = 'https://localhost:3000'
    process.env.PARTICIPANT_URL = 'https://sandbox.demo.localhost:3001'
  }
  if (process.env.CI) {
    await runPopulatePortalScript()
  } else {
    console.log('INFO - Skipping `populate_portal.sh demo ourhealth` -- Non-CI env. assumes demo is already populated')
  }
}

export default globalSetup
