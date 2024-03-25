import { Page } from '@playwright/test'
import { PageComponentInterface } from 'src/models/page-component-interface'

/* footer is located at the bottom of OurHealth page */
export default class Footer implements PageComponentInterface {
  constructor(private readonly page: Page) {
  }

  async isVisible(): Promise<boolean> {
    return Promise.resolve(false)
  }

  async waitReady(): Promise<this> {
    await this.isVisible()
    throw new Error(undefined)
  }
}
