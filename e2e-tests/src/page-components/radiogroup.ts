import { Locator, Page } from '@playwright/test'
import Question from 'src/page-components/question'

export default class Radiogroup {
  private readonly root: Locator

  constructor(private readonly page: Page, private readonly question: Question) {
    this.root = this.question.locator.locator('.sd-question__content')
  }

  get locator(): Locator {
    return this.root
  }

  /** Click a radiogroup item or checkbox **/
  async select(label: string): Promise<this> {
    const radioItem = this.radio(label)
    const checked = await this.isSelected(label)
    if (!checked) {
      await radioItem.click()
    }
    return this
  }

  async isSelected(label: string): Promise<boolean> {
    const clasAttr = await this.radio(label).getAttribute('class')
    return clasAttr?.indexOf('--checked') !== -1
  }

  async allRadioLabels(): Promise<string[]> {
    return this.locator.locator('label').allInnerTexts()
  }

  private radio(label: string | RegExp): Locator {
    if (typeof label === 'string') {
      return this.locator.locator('.sd-radio label')
        .filter({ has: this.page.locator(`xpath=//*[normalize-space()="${label}"]`) })
    }
    return this.locator.locator('.sd-radio label').filter({ hasText: label })
  }
}
