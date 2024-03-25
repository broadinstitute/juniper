import { Locator, Page } from '@playwright/test'
import ComponentBase from 'src/page-components/component-base'

export default class Textbox extends ComponentBase {
  root: Locator

  constructor(page: Page, opts: { label?: string, parent?: Locator }) {
    super(page)
    const { parent, label } = opts
    this.root = parent
      ? label ? parent.locator(`input[aria-label="${label}"]`) :  parent.locator('input[aria-label]').first()
      : label ? this.page.locator(`input[aria-label="${label}"]`)  :  this.page.locator('input[aria-label]').first()
  }

  async fill(value: string | number): Promise<void> {
    const currentValue = await this.currentValue()
    if (!(new RegExp(`^${value.toString()}$`, 'i').test(currentValue))) {
      await this.locator.fill(value.toString())
    }
  }

  async currentValue(): Promise<string> {
    return this.locator.inputValue()
  }
}
