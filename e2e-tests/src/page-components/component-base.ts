import { expect, Locator, Page } from '@playwright/test'
import { PageComponentInterface } from 'src/models/page-component-interface'

export default abstract class ComponentBase implements PageComponentInterface {
  abstract root: Locator

  protected constructor(protected readonly page: Page) {
  }

  get locator(): Locator {
    return this.root
  }

  async isVisible(): Promise<boolean> {
    return this.locator.isVisible()
  }

  async waitReady(): Promise<this> {
    await expect(this.locator).toBeVisible()
    return this
  }
}
