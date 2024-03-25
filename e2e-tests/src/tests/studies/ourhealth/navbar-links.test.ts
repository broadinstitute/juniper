import { expect } from '@playwright/test'
import { test } from 'lib/fixtures/ourhealth-fixture'
import HomeAboutUs from 'src/ourhealth/pages/home-about-us'
import Home from 'src/ourhealth/pages/home'

test.describe('Home page navbar', () => {
  test('navigation', {
    annotation: [
      { type: 'ui component test example', description: 'Click navbar link' }
    ]
  }, async ({ page }) => {
    const home = new Home(page)
    const href = await home.navbar.linkAboutUs.getAttribute(('href'))
    expect(href).toBeTruthy()
    await home.navbar.linkAboutUs.click()

    const aboutUs = new HomeAboutUs(page)
    await aboutUs.waitReady()

    // Back to Home page
    await aboutUs.navbar.linkOurHealthLogo.click()
    await home.waitReady()

    // Loading /aboutUs URL
    await home.goTo(href!)
    await aboutUs.waitReady()
  })
})

