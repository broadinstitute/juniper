import {
  expect,
  Locator,
  Page
} from '@playwright/test'
import WebComponentBase from 'src/page-components/web-component-base'
import Radiogroup from 'src/page-components/radiogroup'
import Textbox from 'src/page-components/textbox'

export type QuestionOpts = {
  qText?: string | RegExp,
  dataName?: string,
  parent?: string,
  exact?: boolean
}

export default class Question extends WebComponentBase {
  root: Locator

  /**
   * @param {Page} page
   * @param {{parent?: string, qText?: string | RegExp, dataName?: string}} opts
   */
  constructor(page: Page, opts: QuestionOpts = {}) {
    super(page)

    const { dataName } = opts
    const defaultRoot = '.sd-page'

    this.root = this.page.locator(defaultRoot).locator(`[data-name="${dataName}"]`)
    return
  }

  async error(): Promise<string | null> {
    const err = this.locator.locator('[role="alert"].sd-question__erbox')
    try {
      await expect(err).toBeVisible({ timeout: 1000 })
      return err.innerText()
    } catch (err) {
      /* empty */
    }
    return null
  }

  /** Get question texts or title */
  async texts(): Promise<string> {
    this.title = await (this.locator.locator('.sd-question__title .sv-string-viewer')).innerText()
    return this.title
  }

  async isRequired(): Promise<boolean> {
    const attr = await this.locator.getAttribute('aria-required')
    return attr ? attr === 'true' : false
  }

  async select(label: string): Promise<this> {
    await new Radiogroup(this.page, this).select(label)
    return this
  }

  async fillIn(value: string): Promise<this> {
    await new Textbox(this.page, { parent: this.locator }).fill(value)
    return this
  }


  async fillAllInWith(value: string): Promise<this> {
    const allInputs = await this.locator.locator('input').all()

    for (const input of allInputs) {
      await input.fill(value)
    }

    return this
  }
}
