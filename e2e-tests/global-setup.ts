import { exec } from 'child_process'

const runPopulatePortalScript = (): Promise<undefined> => {
  return new Promise((resolve, reject) => {
    exec('../scripts/populate_portal.sh ourhealth', (err, stdout, stderr) => {
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
    process.env.PARTICIPANT_URL = 'http://sandbox.ourhealth.localhost:8081'
  } else {
    process.env.ADMIN_URL = 'http://localhost:3000'
    process.env.PARTICIPANT_URL = 'http://sandbox.ourhealth.localhost:3001'
  }

  await runPopulatePortalScript()
}

export default globalSetup
