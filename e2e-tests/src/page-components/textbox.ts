import { Locator, Page } from '@playwright/test'
import WebElementBase from 'src/page-components/web-element-base'

/**
 * A web element.
 */
export default class Textbox extends WebElementBase {
  root: Locator

  constructor(page: Page, opts: { name?: string, parent?: Locator }) {
    super(page)
    const { parent, name } = opts

    this.root = parent
      ? name
        ? parent.locator(`xpath=//input[@aria-label="${name}" or @placeholder="${name}"]`)
        : parent.locator('input[aria-label], input[placeholder]').first()
      : name
        ? this.page.locator(`xpath=//input[@aria-label="${name}" or @placeholder="${name}"]`)
        : this.page.locator('input[aria-label], input[placeholder]').first()
  }

  async fill(value: string | number): Promise<void> {
    const currentValue = await this.currentValue()
    if (!(new RegExp(`^${value.toString()}$`, 'i').test(currentValue))) {
      await this.locator.fill(value.toString())
      await this.locator.blur()
    }
  }

  async currentValue(): Promise<string> {
    return await this.locator.inputValue()
  }
}
