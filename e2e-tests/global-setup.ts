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

const globalSetup = async () => {
  process.env.ADMIN_URL = 'http://localhost:8080'
  process.env.PARTICIPANT_URL = 'http://sandbox.ourhealth.localhost:8081'

  await runPopulatePortalScript()
}

export default globalSetup
