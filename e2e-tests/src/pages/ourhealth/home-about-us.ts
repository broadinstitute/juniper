import { expect, Locator, Page } from '@playwright/test'
import PageBase from 'pages/ourhealth/page-base'

export  default  class HomeAboutUs extends PageBase {
  title = 'About Us | OurHealth'

  // Locators to find web elements
  linkLearnMoreAboutParticipation: Locator

  linkScientificBackground: Locator

  constructor(page: Page) {
    super(page)
    this.linkLearnMoreAboutParticipation = this.page.locator('a.nav-link',
      { hasText: /^Learn More About Participation$/ })
    this.linkScientificBackground = this.page.locator('a.btn', { hasText: new RegExp(/^Scientific Background$/) })
  }

  /* Minimum checks to ensure the page is loaded */
  async waitReady(): Promise<void> {
    await super.waitReady()
    await expect(this.page).toHaveURL(/\/aboutUs$/)
  }
}
