import { expect, Locator, Page } from '@playwright/test'
import OurHealthPageBase from 'pages/ourhealth/ourhealth-page-base'

export  default  class HomeAboutUs extends OurHealthPageBase {
  title = 'About Us | OurHealth'

  // Locators to find web elements
  linkLearnAboutParticipation: Locator

  linkScientificBackground: Locator

  constructor(page: Page) {
    super(page)
    this.linkLearnAboutParticipation = this.page.locator('a.nav-link', { hasText: /^Learn More About Participation$/ })
    this.linkScientificBackground = this.page.locator('a.btn', { hasText: new RegExp(/^Scientific Background$/) })
  }

  /* Minimum checks to ensure the page is loaded */
  async waitReady(): Promise<this> {
    await super.waitReady()
    await expect(this.page).toHaveURL(/\/aboutUs$/)
    return this
  }
}
