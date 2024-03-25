import { expect, Page } from '@playwright/test'
import StudyEligibility from 'pages/ourhealth/study-eligibility'
import Navbar from 'src/page-components/navbar'

/**
 * Generate a random alphanumerical string
 */
export function randomChars(length: number) {
  let result = ''
  const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  const charactersLength = characters.length
  let counter = 0
  while (counter < length) {
    result += characters.charAt(Math.floor(Math.random() * charactersLength))
    counter += 1
  }
  return result
}


/**
 * login as the given admin user using development mode.
 * This should eventually handle checking to see if the user is already logged in.
 */
export async function adminLogin(page: Page, username?: string) {
  if (!username) {
    username = 'dbush@broadinstitute.org'
  }
  await page.goto(`${process.env.ADMIN_URL}`)
  await expect(page).toHaveTitle('Juniper')
  await page.locator('button:text("developer login")').click()
  await page.locator('input[id="inputLoginEmail"]').fill(username)
  await page.locator('button:text("Log in")').click()
  // the after login page might be either the participant list or the home page depending on the number of portals
  await Promise.any([
    expect(page.locator('h1')).toHaveText('Juniper Home'),
    expect(page.locator('h2')).toHaveText('Participant List')
  ])

  return page
}

/**
 * Click Register button on Home page to go to the Study Eligibility page.
 * @param {Page} page
 * @returns {Promise<StudyEligibility>}
 */
export async function goToStudyEligibility(page: Page): Promise<StudyEligibility> {
  const navbar = new Navbar(page)
  await navbar.linkRegister.click()
  const prequal = new StudyEligibility(page)
  await  prequal.waitReady()
  return prequal
}

/**
 * Create an alias email.
 * Example: emailAlias('tonyw@broadinstitute.org') => tonyw+1123456789@broadinstitute.org
 */
export function emailAlias(email: string): string {
  if (email.length === 0 || !email.includes('@')) {
    throw new Error(`Invalid email format: "${email}"`)
  }
  const splintedEmail = email.split('@')
  const name = splintedEmail[0]
  const domain = splintedEmail[1]
  return `${name}+${Math.floor(Math.random() * 1000000000)}@${domain}`
}
