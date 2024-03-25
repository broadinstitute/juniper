import { expect, Locator, Page } from '@playwright/test'
import { RegisterPageInterface } from 'src/models/register-page-interface'
import Footer from 'src/page-components/footer'
import Navbar from 'src/page-components/navbar'
import Question from 'src/page-components/question'

export default abstract class PageBase implements RegisterPageInterface {
  protected readonly page: Page

  title = 'OurHealth'

  navbar: Navbar

  footer: Footer

  protected constructor(page: Page) {
    this.page = page
    this.navbar = new Navbar(page)
    this.footer = new Footer(page)
  }

  async waitReady(): Promise<void> {
    await expect(this.page).toHaveTitle(this.title)
  }

  async goTo(path: string): Promise<void> {
    await this.page.goto(path)
  }

  /** Click +/- icon to expand or collapse hidden texts */
  async togglePlusMinus(textLabel: string, opts: { expand?: boolean } = {}): Promise<string | null> {
    const { expand = true } = opts
    const btn = this.page.locator('//button[.//*[@data-icon="plus"]' +
        ` and contains(normalize-space(), "${textLabel}")` +
        ' and @aria-expanded and @aria-controls]')
    const ariaControlId = await btn.getAttribute('aria-controls')
    const isExpanded: boolean = await btn.getAttribute('aria-expanded') === 'true'
    if (expand) {
      if (!isExpanded) {
        await btn.click()
        await expect(btn).toHaveAttribute('aria-expanded', 'true')
      }
    } else {
      if (isExpanded) {
        await btn.click()
        await expect(btn).toHaveAttribute('aria-expanded', 'false')
      }
    }
    return ariaControlId
  }

  // TODO
  // eslint-disable-next-line @typescript-eslint/require-await
  async check(question: string, value: string): Promise<this> {
    throw new Error('undefined')
  }

  /** Need more roles? See all roles on https://playwright.dev/docs/api/class-locator#locator-get-by-role  **/
  async click(role: 'button' | 'checkbox' | 'link' | 'radiogroup', name: string | RegExp): Promise<this> {
    await this.page.getByRole(role, { name }).click()
    return this
  }

  async fillIn(question: string, value: string): Promise<this> {
    const q = new Question(this.page, { qText: question })
    await q.fillIn(value)
    return this
  }

  async select(question: string, value: string): Promise<this> {
    const q = new Question(this.page, { qText: question })
    await q.select(value)
    return this
  }

  async drawLine(question: string): Promise<this> {
    const q = new Question(this.page, { qText: question })
    const canvas = q.locator.locator('canvas')
    await canvas.scrollIntoViewIfNeeded()
    const box = await canvas.boundingBox()
    if (!box) {
      throw new Error('canvas boundingBox is null')
    }

    await this.page.mouse.move(box.x + 50, box.y + 50)
    await this.page.mouse.down()
    for (let i = 0; i < 20; ++i) {
      await this.page.mouse.move(box.x, box.y, { steps: 1.1 })
    }
    await this.page.mouse.up()
    return this
  }

  /** Click the Submit button **/
  async submit(): Promise<void> {
    await this.click('button', 'Submit')
  }

  async progress(): Promise<string> {
    return this.formProgress.locator('.sd-progress__text:visible').innerText()
  }

  /**
   * Select either "I agree" and "I do not agree" radiobutton
   * @returns {Promise<this>}
   * @param yes True to click "I agree" radiobutton.
   */
  async agree(yes = true): Promise<this> {
    const label = yes ? 'I agree' : 'I do not agree'
    await this.page.locator(`label[aria-label="${label}"]`).click()
    return this
  }

  protected get questionRoot(): Locator {
    return this.formBody.locator('.sd-row > [data-key]')
  }

  protected get formTitle(): Locator {
    return this.formBody.locator('.sd-page__title')
  }

  protected get formDescription(): Locator {
    return this.formBody.locator('.sd-page__description')
  }

  protected get formBody(): Locator {
    return this.page.locator('.sd-body__page')
  }

  protected get formProgress(): Locator {
    return this.page.locator('.sd-progress')
  }

  protected get formActionbar(): Locator {
    return this.page.locator('.sd-action-bar.sd-footer')
  }

  divFor(header: string): Locator {
    return this.page.locator(`//div[./h2[contains(normalize-space(), "${header}")]]`)
  }
}
