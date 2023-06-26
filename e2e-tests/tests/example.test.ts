import { test, expect } from '@playwright/test'

test('has title', async ({ page }) => {
  await page.goto(`${process.env.PARTICIPANT_URL}/`)

  await expect(page).toHaveTitle('OurHealth')
})
