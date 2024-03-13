import { Fixtures, test as base } from '@playwright/test'

// Use this fixture for login in ui-admin tests
export const fixtureBase = base.extend<Fixtures>({
  page: async ({ page }, use) => {
    // Listen for console events and only write errors to the Terminal window
    page.on('console', msg => {
      if (msg.type() === 'error') {
        console.log(`Browser Console Error: "${msg.text()}"`)
      }
    })
    await use(page)
  }
})
