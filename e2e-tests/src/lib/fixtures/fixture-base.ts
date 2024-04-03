import { Fixtures, test as base, expect } from '@playwright/test'

/**
 * Base fixture to be extended by fixtures of studies
 */
export const fixtureBase = base.extend<Fixtures>({
  page: async ({ page }, use) => {
    // Listen for console events and write errors to the Terminal window
    page.on('console', msg => {
      if (msg.type() === 'error') {
        console.log(`Browser Console\n***\n"${msg.text()}"\n***\n\n`)
      }
    })

    // Experimental feature. https://playwright.dev/docs/api/class-page#page-add-locator-handler
    // Check for loading spinner and wait for hidden
    await page.addLocatorHandler(page.locator('body'), async () => {
      await expect(page.getByTestId('loading-spinner')).toBeHidden({ timeout: 30 * 1000 })
    })

    await use(page)
  }
})
