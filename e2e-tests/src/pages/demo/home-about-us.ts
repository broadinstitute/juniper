import { expect, Locator, Page } from '@playwright/test'
import data from 'src/data/ourhealth-en.json'
import DemoPageBase from 'pages/demo/demo-page-base'

export default class HomeAboutUs extends DemoPageBase {
  title = 'About Us | Juniper Demo'

  // Locators to find web elements
  linkLearnAboutParticipation: Locator

  linkScientificBackground: Locator

  constructor(page: Page) {
    super(page)
    this.linkLearnAboutParticipation = this.page.locator('a.nav-link', { hasText: data.Link.LearnMore })
    this.linkScientificBackground = this.page.locator('a.btn', { hasText: data.Link.ScientificBackground })
  }

  /* Minimum checks to ensure the page is loaded */
  async waitReady(): Promise<this> {
    await super.waitReady()
    await expect(this.page).toHaveURL(/\/aboutUs$/)
    return this
  }
}
