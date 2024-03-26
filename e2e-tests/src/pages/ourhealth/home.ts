import { Page, expect, Locator } from '@playwright/test'
import OurHealthPageBase from 'pages/ourhealth/ourhealth-page-base'
import { Study } from 'src/data/constants-en'

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
    this.linkBecomeParticipant = this.page.locator('a.btn', { hasText: Study.OurHealth.Link.BecomeParticipant })
    this.linkScientificBackground = this.page.locator('a.btn', { hasText: Study.OurHealth.Link.ScientificBackground })
    this.linkGetStarted = this.page.locator('a.btn', { hasText: Study.OurHealth.Link.GetStarted })
    this.linkLearnMoreAboutParticipation = this.page.locator('a.btn', { hasText: Study.OurHealth.Link.LearnMore })
    this.linkMoreFAQ = this.page.locator('a.btn', { hasText: Study.OurHealth.Link.MoreFAQ })
    this.linkJoinMailingList = this.page.locator('a.btn', { hasText: Study.OurHealth.Link.JoinMailingList })
    this.linkJoinOurHealth = this.page.locator('a.btn', { hasText: Study.OurHealth.Link.Join })
  }

  /* Minimum checks to ensure the page is loaded */
  async waitReady(): Promise<this> {
    await super.waitReady()
    await this.navbar.isVisible()
    await expect(this.linkBecomeParticipant).toBeEnabled()
    return this
  }
}
