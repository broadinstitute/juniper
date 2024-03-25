import { Fixtures, test as base } from '@playwright/test'

export const fixtureBase = base.extend<Fixtures>({
  page: async ({ page }, use) => {
    // Listen for console events and write errors to the Terminal window
    page.on('console', msg => {
      if (msg.type() === 'error') {
        console.log(`Browser Console\n***\n"${msg.text()}"\n***\n\n`)
      }
    })
    await use(page)
  }
})
