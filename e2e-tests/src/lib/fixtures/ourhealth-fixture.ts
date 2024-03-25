import { Fixtures, expect } from '@playwright/test'
import { fixtureBase as base } from 'src/lib/fixtures/fixture-base'
import Home from 'src/ourhealth/pages/home'

// Use this fixture for login in studies tests
export const test = base.extend<Fixtures>({
  page: async ({ baseURL, page }, use) => {
    await page.goto(baseURL!)
    const home = new Home(page)
    await home.waitReady()

    try {
      // Dismiss Cookies alert if present
      const alert = page.locator('//*[contains(@class, "alert-heading") and contains(text(), "Cookies")]')
      await expect(alert).toBeVisible({ timeout: 3000 })
      await page.locator('//*[@role="alert"]//button[@aria-label="Close"]').click()
    } catch (error) {
      /* empty */
    }

    await use(page)
  }
})

