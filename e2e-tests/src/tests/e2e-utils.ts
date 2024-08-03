import { faker } from '@faker-js/faker'
import { errors, expect, Page, Response, test } from '@playwright/test'
import StudyEligibilityOurHealth from 'pages/ourhealth/study-eligibility'
import StudyEligibilityDemo from 'pages/demo/study-eligibility'
import Navbar from 'src/page-components/navbar'

export type Study = 'OurHealth';

export type Environment = 'local' | 'dev'

interface NetworkResponse {
  uri: string;
  status?: number;
  timeout?: number;
}

/**
 * Generate a random alphanumerical string
 */
export const randomChars = (length: 6): string => {
  return faker.string.alphanumeric(length)
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
    expect(page.locator('h1')).toHaveText('Select a portal'),
    expect(page.locator('h2')).toHaveText('Participant List')
  ])

  return page
}

/**
 * Click Register button on Home page to go to the Study Eligibility page.
 * @param {Page} page
 * @returns {Promise<StudyEligibility>}
 */
export async function goToOurhealthPreEnroll(page: Page): Promise<StudyEligibilityOurHealth> {
  const navbar = new Navbar(page)
  await navbar.linkRegister.click()
  const prequal = new StudyEligibilityOurHealth(page)
  await prequal.waitReady()
  return prequal
}


/**
 * Click Register button on Home page to go to the Study Eligibility page.
 * @param {Page} page
 * @returns {Promise<StudyEligibility>}
 */
export async function goToDemoPreEnroll(page: Page): Promise<StudyEligibilityDemo> {
  const navbar = new Navbar(page)
  await navbar.linkRegister.click()
  const prequal = new StudyEligibilityDemo(page)
  await prequal.waitReady()
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

/**
 *
 */
export function localTime() {
  return new Date().toLocaleTimeString()
}

/**
 *
 */
export function logError(err: string) {
  test.info().annotations.push({
    type: 'Error',
    description: `:x: @${localTime()}: ${err}`
  })
}

/**
 *
 */
export function logInfo(info: string) {
  test.info().annotations.push({
    type: 'Info',
    description: `:heavy_check_mark: @${localTime()}: ${info}`
  })
}

/**
 *
 */
export async function waitForResponse(page: Page, { uri, status = 200, timeout }: NetworkResponse): Promise<Response> {
  let response: Response
  try {
    response = await page.waitForResponse((resp: Response) => resp.url().includes(uri), { timeout })
    await response.finished()
  } catch (err) {
    if (err instanceof errors.TimeoutError) {
      throw new Error(`TimeoutError: waiting for URI: ${uri}: Timeout exceeded`)
    }
    throw err
  }
  const respStatus = response.status()
  if (respStatus === status) {
    return response
  }
  const url = response.url()
  const method = response.request().method()
  const body = await response.text()
  const reqPayload = response.request().postData() || ''

  throw new Error(
      `Waiting for URI: ${uri} with status: ${status}.\n  ` +
      `${method} ${url}\n  Status: ${respStatus}\n  Text: ${body}\n  Payload: ${reqPayload}`)
}
