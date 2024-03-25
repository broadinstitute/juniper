import { Locator, Page } from '@playwright/test'

export default class Textbox {
  private readonly root: Locator

  constructor(private readonly page: Page, opts: { label?: string, parent?: Locator }) {
    const { parent, label } = opts
    this.root = parent
      ? label ? parent.locator(`input[aria-label="${label}"]`) :  parent.locator('input[aria-label]').first()
      : label ? this.page.locator(`input[aria-label="${label}"]`)  :  this.page.locator('input[aria-label]').first()
  }

  get locator(): Locator {
    return this.root
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
