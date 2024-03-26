import { Fixtures, expect } from '@playwright/test'
import { fixtureBase as base } from 'src/lib/fixtures/fixture-base'
import Home from 'pages/ourhealth/home'

// Use this fixture in OurHealth study tests
export const test = base.extend<Fixtures>({
  page: async ({ baseURL, page }, use) => {
    await page.goto(baseURL!)
    const home = new Home(page)
    await home.waitReady()

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

