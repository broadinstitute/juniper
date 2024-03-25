import { Page } from '@playwright/test'
import { PageComponentInterface } from 'pages/page-component-interface'

export default class Footer implements PageComponentInterface {
  constructor(private readonly page: Page) {
  }

  async isVisible(): Promise<boolean> {
    return Promise.resolve(false)
  }

  async waitReady(): Promise<void> {
    return Promise.resolve(undefined)
  }
}
