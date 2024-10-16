import { Locator, Page } from '@playwright/test'
import WebElementBase from 'src/page-components/web-element-base'
import Question from 'src/page-components/question'

/**
 * A web element.
 */
export default class Radiogroup extends  WebElementBase {
  root: Locator

  constructor(page: Page, private readonly question: Question) {
    super(page)
    this.root = this.question.locator.locator('.sd-question__content')
  }

  /** Click a radiogroup item or checkbox **/
  async select(label: string | RegExp): Promise<this> {
    const radioItem = this.radioButton(label)
    const checked = await this.isSelected(label)
    if (!checked) {
      await radioItem.click()
    }
    return this
  }

  async isSelected(label: string | RegExp): Promise<boolean> {
    const clasAttr = await this.radioButton(label).getAttribute('class')
    return clasAttr?.indexOf('--checked') !== -1
  }

  async allRadioLabels(): Promise<string[]> {
    return await this.locator.locator('label').allInnerTexts()
  }

  radioButton(label: string | RegExp): Locator {
    if (typeof label === 'string') {
      return this.locator.locator('.sd-radio label')
        .filter({ has: this.page.locator(`xpath=//*[normalize-space()="${label}"]`) })
    }
    return this.locator.locator('.sd-radio label').filter({ hasText: label })
  }
}
