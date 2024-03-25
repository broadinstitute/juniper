import { Page, expect, Locator } from '@playwright/test'
import PageBase from 'pages/ourhealth/page-base'

export enum Label {
  HelpUsUnderstand = 'Help us understand cardiovascular disease risk among South Asian populations.',
  WhyIsNeeded = 'Why is this needed?',
  WhoCanJoin = 'Who can join?',
  HowToParticipant = 'How to Participate',
  FAQ = 'Frequently Asked Questions'
}

export  default  class Home extends PageBase {
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
    this.linkBecomeParticipant = this.page.locator('a.btn', { hasText: /^Become a Participant$/ })
    this.linkScientificBackground = this.page.locator('a.btn', { hasText: /^Scientific Background$/ })
    this.linkGetStarted = this.page.locator('a.btn', { hasText: /^Get Started$/ })
    this.linkLearnMoreAboutParticipation = this.page.locator('a.btn', { hasText: /^Learn More About Participation$/ })
    this.linkMoreFAQ = this.page.locator('a.btn', { hasText: /^More FAQs$/ })
    this.linkJoinMailingList = this.page.locator('a.btn', { hasText: /^Join Mailing List$/ })
    this.linkJoinOurHealth = this.page.locator('a.btn', { hasText: /^Join OurHealth$/ })
  }

  /* Minimum checks to ensure the page is loaded */
  async waitReady(): Promise<void> {
    await super.waitReady()
    await this.navbar.isVisible()
    await expect(this.linkBecomeParticipant).toBeEnabled()
  }
}
