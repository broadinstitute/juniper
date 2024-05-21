import { Page, expect, Locator } from '@playwright/test'
import OurHealthPageBase from 'pages/ourhealth/ourhealth-page-base'
import data from 'src/data/ourhealth-en.json'

export  default  class Home extends OurHealthPageBase {
  // Locators to find web elements
  linkBecomeParticipant: Locator

  linkScientificBackground: Locator

  linkGetStarted: Locator

  linkLearnMoreAboutParticipation: Locator

  linkMoreFAQ: Locator

  linkJoinMailingList: Locator

  linkJoinOurHealth: Locator

  constructor(page: Page) {
    super(page)
    this.linkBecomeParticipant = this.page.locator('a.btn', { hasText: data.Link.BecomeParticipant })
    this.linkScientificBackground = this.page.locator('a.btn', { hasText: data.Link.ScientificBackground })
    this.linkGetStarted = this.page.locator('a.btn', { hasText: data.Link.GetStarted })
    this.linkLearnMoreAboutParticipation = this.page.locator('a.btn', { hasText: data.Link.LearnMore })
    this.linkMoreFAQ = this.page.locator('a.btn', { hasText: data.Link.MoreFAQ })
    this.linkJoinMailingList = this.page.locator('a.btn', { hasText: data.Link.JoinMailingList })
    this.linkJoinOurHealth = this.page.locator('a.btn', { hasText: data.Link.Join })
  }

  /* Minimum checks to ensure the page is loaded */
  async waitReady(): Promise<this> {
    await super.waitReady()
    await this.navbar.isVisible()
    await expect(this.linkBecomeParticipant).toBeEnabled()
    return this
  }
}
