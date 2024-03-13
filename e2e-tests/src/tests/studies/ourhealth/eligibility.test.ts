import { test } from 'lib/fixtures/ourhealth-fixture'

test.describe('experience', () => {
  test('one', async ({ page }) => {
    console.log(page.title())
  })
})
