import { Fixtures } from '@playwright/test'
import { fixtureBase as base } from 'src/lib/fixtures/fixture-base'
import Home from 'pages/ourhealth/home'

// Use this fixture in OurHealth study tests
export const test = base.extend<Fixtures>({
  page: async ({ baseURL, page }, use) => {
    await page.goto(baseURL!)
    const home = new Home(page)
    await home.waitReady()

    await use(page)
  }
})

