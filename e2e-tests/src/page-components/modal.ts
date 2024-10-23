import {
  Locator,
  Page
} from '@playwright/test'
import WebComponentBase from 'src/page-components/web-component-base'

export default class Modal extends WebComponentBase {
  root: Locator

  constructor(page: Page, opts: { label?: string, nth?: number } = {}) {
    super(page)
    const { label, nth = 0 } = opts

    this.root = label
      ? this.page.locator(`[role="dialog"][aria-label="${label}" i]`).nth(nth) // case-insensitive
      : this.page.locator('[role="dialog"]').nth(nth)
  }

  async close(): Promise<void> {
    await this.locator.getByRole('button', { name: 'Close' }).click()
  }

  async fillIn(value: string): Promise<this> {
    await super.fillIn(value, this.locator)
    return this
  }
}
