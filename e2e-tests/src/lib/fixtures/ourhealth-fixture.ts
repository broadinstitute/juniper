import { Fixtures } from '@playwright/test'
import { fixtureBase as base } from 'src/lib/fixtures/fixture-base'

// Use this fixture for login in DSM tests
export const test = base.extend<Fixtures>({
  page: async ({ baseURL, page }, use) => {
    await page.goto(`${baseURL}/?showJoinMailingList=true `)
    await use(page)
  }
})

export { expect } from '@playwright/test'
