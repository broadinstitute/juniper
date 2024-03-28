import { expect, Locator, Page } from '@playwright/test'
import JuniperPageBase from 'src/models/juniper-page-base'
import { WebElementInterface } from 'src/models/web-element-interface'

export default abstract class WebComponentBase extends JuniperPageBase implements WebElementInterface {
  abstract root: Locator

  title = 'undefined' // Set to undefined to ignore. Inherited from JuniperPageInterface.

  protected constructor(protected readonly page: Page) {
    super(page)
  }

  async isVisible(): Promise<boolean> {
    return await this.locator.isVisible()
  }

  get locator(): Locator {
    return this.root
  }

  async waitReady(): Promise<this> {
    await expect(this.locator).toBeVisible()
    return this
  }
}
