import { Locator, Page, expect } from '@playwright/test'
import { PageComponentInterface } from 'src/models/page-component-interface'
import Radiogroup from 'src/page-components/radiogroup'
import Textbox from 'src/page-components/textbox'

export default class Question implements PageComponentInterface {
  private readonly root: Locator

  /**
   *
   * @param {Page} page
   * @param {{parent?: string, qText?: string | RegExp, dataName?: string}} opts
   */
  constructor(private readonly page: Page, opts: {
    readonly qText?: string | RegExp,
    dataName?: string,
    readonly parent?: string } = {}) {
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

  get locator(): Locator {
    return this.root
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

  async isVisible(): Promise<boolean> {
    return this.locator.isVisible()
  }

  async waitReady(): Promise<void> {
    return Promise.resolve(undefined)
  }

  /** Question texts */
  async title(): Promise<string> {
    return (this.locator.locator('.sd-question__title .sv-string-viewer')).innerText()
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
        break
    }
    return this
  }

  /**
   * Gets value in role attribute
   * @returns {Promise<string>} radiogroup, input, etc.
   */
  private async role(): Promise<string> {
    const role = await this.locator.getAttribute('role')
    if (role) {
      return role
    }
    throw new Error(`Unable to get attribute: "role" for question. ${this.locator}`)
  }
}
