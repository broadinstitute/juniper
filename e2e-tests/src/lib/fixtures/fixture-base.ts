import { expect, Fixtures, test as base } from '@playwright/test'

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

    // Dismiss Cookies alert
    try {
      const alert = page.locator('//*[contains(@class, "alert-heading") and contains(text(), "Cookies")]')
      await expect(alert).toBeVisible({ timeout: 2000 })
      await page.locator('//*[@role="alert"]//button[@aria-label="Close"]').click()
    } catch (error) {
      /* empty */
    }

    await use(page)
  }
})
