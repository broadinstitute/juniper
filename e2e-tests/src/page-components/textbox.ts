import {
  Locator,
  Page
} from '@playwright/test'
import WebElementBase from 'src/page-components/web-element-base'

/**
 * A web element.
 */
export default class Textbox extends WebElementBase {
  root: Locator

  constructor(page: Page, opts: { name?: string, parent: Locator }) {
    super(page)
    const { parent, name } = opts
    this.root = name ? parent.locator(`input[name="${name}"]`) : parent.locator('input')
  }

  async fill(value: string | number): Promise<void> {
    await this.locator.fill(value.toString())
    await this.locator.blur()
  }
}
