import { expect } from '@playwright/test'
import { test } from 'lib/fixtures/ourhealth-fixture'
import HomeAboutUs from 'pages/ourhealth/home-about-us'
import Home from 'pages/ourhealth/home'

test.describe('Home page navbar', () => {
  test('navigation', {
    annotation: [
      { type: 'page component unit-test example', description: 'Click navbar link' }
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

    // Loading href in URL directly
    await home.goTo(href!)
    await aboutUs.waitReady()
  })
})

