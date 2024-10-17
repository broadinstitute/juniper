import {
  expect,
  Locator,
  Page,
  Response
} from '@playwright/test'
import { JuniperPageInterface } from 'src/models/juniper-page-interface'
import Textbox from 'src/page-components/textbox'

export default abstract class JuniperPageBase implements JuniperPageInterface {
  abstract title: string

  protected constructor(protected readonly page: Page) {
  }

  async goTo(path: string): Promise<Response | null> {
    return await this.page.goto(path)
  }

  async waitReady(): Promise<this> {
    await expect(this.page).toHaveTitle(this.title)
    return this
  }

  /**
   * See clickByRole() if this doesn't work.
   * @param {string} name
   * @returns {Promise<this>}
   */
  async clickByName(name: string): Promise<this> {
    // eslint-disable-next-line max-len
    const link = this.page.locator(`xpath=(//a | //button | //input[@type="button"])[normalize-space()="${name}" or @aria-label="${name}" or @title="${name}"]`)
    await link.first().click()
    return this
  }


  /** Need more roles? See all roles on https://playwright.dev/docs/api/class-locator#locator-get-by-role  **/
  async clickByRole(role: 'button' | 'checkbox' | 'link' | 'radiogroup', name: string | RegExp, options: {
    exact?: boolean
  } = {}): Promise<this> {
    const link = this.page.getByRole(role, { name, exact: options.exact !== undefined ? options.exact : true })
    await link.click()
    return this
  }

  async fillIn(value: string, parent: Locator): Promise<this> {
    const textbox = new Textbox(this.page, { parent })
    await textbox.fill(value)
    return this
  }
}
