import { expect, Locator, Page } from '@playwright/test'
import { PageComponentInterface } from 'src/models/page-component-interface'

/* navbar is located at the top of OurHealth page */
export default class Navbar implements PageComponentInterface {
  private readonly root: Locator

  linkOurHealthLogo: Locator

  linkAboutUs: Locator

  linkFAQ: Locator

  linkParticipation: Locator

  linkScientificBackground: Locator

  linkRegister: Locator

  linkLogIn: Locator


  constructor(private readonly page: Page) {
    this.root = this.page.locator('.navbar')
    this.linkOurHealthLogo = this.root.locator('a.navbar-brand[href*="/"]')
    this.linkAboutUs = this.root.locator('a.nav-link', { hasText: new RegExp(/^About Us$/) })
    this.linkFAQ = this.root.locator('a.nav-link', { hasText: new RegExp(/^FAQ$/) })
    this.linkParticipation = this.root.locator('a.nav-link', { hasText: new RegExp(/^Participation$/) })
    this.linkScientificBackground = this.root.locator('a.nav-link', { hasText: new RegExp(/^Scientific Background$/) })
    this.linkRegister = this.root.locator('a.btn', { hasText: new RegExp(/^Register$/) })
    this.linkLogIn = this.root.locator('a.btn', { hasText: new RegExp(/^Log In$/) })
  }

  async isVisible(): Promise<boolean> {
    return this.root.isVisible()
  }

  async waitReady(): Promise<this> {
    await expect(this.linkOurHealthLogo).toBeEnabled()
    return this
  }
}
