import { Page, expect, Locator } from '@playwright/test'
import WebComponentBase from 'src/page-components/web-component-base'
import Radiogroup from 'src/page-components/radiogroup'
import Textbox from 'src/page-components/textbox'

export default class Question extends WebComponentBase {
  root: Locator

  /**
   * @param {Page} page
   * @param {{parent?: string, qText?: string | RegExp, dataName?: string}} opts
   */
  constructor(page: Page, opts: { qText?: string | RegExp, dataName?: string, parent?: string } = {}) {
    super(page)

    const { parent, qText, dataName } = opts
    const defaultRoot = 'form .sd-page__row > [data-key] > [id][role]'

    if (dataName) {
      // If the data-name attribute is used, we don't have to check other parameters because
      // it should be a unique value like the id attribute. But unlike the id attribute, data-name is static.
      this.root = this.page.locator(defaultRoot).filter({ has: this.page.locator(`[data-name="${dataName}"]`) })
      return
    }

    this.root = parent
      ? this.page.locator(parent).locator(defaultRoot)
      : this.page.locator(defaultRoot)

    if (qText) {
      this.root = typeof qText === 'string'
        ? this.root.filter({
          has: this.page.locator(
            `xpath=//*[contains(@class, "sv-string-viewer") and contains(normalize-space(), "${qText}")]`)
        })
        : this.root.filter({ has: this.page.locator('.sv-string-viewer', { hasText: qText }) })
    }
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

  async select(value: string): Promise<this> {
    const qRole = await this.role()
    switch (qRole) {
      case 'radiogroup':
        await new Radiogroup(this.page, this).select(value)
        break
      default:
        break
    }
    return this
  }

  async fillIn(value: string): Promise<this> {
    const qRole = await this.role()
    switch (qRole) {
      case 'textbox':
        await new Textbox(this.page, { parent: this.locator }).fill(value)
        break
      default:
        throw new Error(`undefined case: ${qRole}`)
    }
    return this
  }

  /**
   * Gets value in role attribute
   * @returns {Promise<string>} radiogroup, textbox, etc.
   */
  private async role(): Promise<string> {
    const role = await this.locator.getAttribute('role')
    if (role) {
      return role
    }
    throw new Error(`Unable to get attribute: "role" for question. ${this.locator}`)
  }
}
