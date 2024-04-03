import { expect, Locator, Page } from '@playwright/test'
import { WebElementInterface } from 'src/models/web-element-interface'

export default abstract class WebElementBase implements WebElementInterface {
  abstract root: Locator

  protected constructor(protected readonly page: Page) {
  }

  get locator(): Locator {
    return this.root
  }

  async isVisible(): Promise<boolean> {
    return await this.locator.isVisible()
  }

  async waitReady(): Promise<this> {
    await expect(this.locator).toBeVisible()
    return this
  }
}
