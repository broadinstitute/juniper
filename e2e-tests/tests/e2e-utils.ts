import { expect, Page } from '@playwright/test'

/**
 *
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
