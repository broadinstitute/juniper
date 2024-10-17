import {
  Locator,
  Page
} from '@playwright/test'
import WebElementBase from 'src/page-components/web-element-base'
import Question from 'src/page-components/question'

/**
 * A web element.
 */
export default class Radiogroup extends  WebElementBase {
  root: Locator

  constructor(page: Page, private readonly question: Question) {
    super(page)
    this.root = this.question.locator
  }

  /** Click a radiogroup item or checkbox **/
  async select(label: string): Promise<this> {
    const radioItem = this.radioButton(label)
    await radioItem.click()
    return this
  }

  radioButton(label: string): Locator {
    // normally, you could getByLabel to get the actual
    // underlying input. however, in this case, surveyjs'
    // label does some weird onclick listening that doesn't
    // get fired if you interact directly with the input
    return this.locator.getByText(label)
  }
}
