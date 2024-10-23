import {
  expect,
  Locator,
  Page,
  Response
} from '@playwright/test'
import JuniperPageBase from 'src/models/juniper-page-base'
import { RegistrationPageInterface } from 'src/models/registration-page-interface'
import Question, { QuestionOpts } from 'src/page-components/question'

export default abstract class RegistrationPageBase extends JuniperPageBase implements RegistrationPageInterface {
  protected constructor(protected readonly page: Page) {
    super(page)
  }

  getQuestion(dataName: string, opts?: QuestionOpts): Question {
    return new Question(this.page, { ...opts, dataName })
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

  async drawSignature(question: string): Promise<this> {
    const q = new Question(this.page, { dataName: question })
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
  async submit(opts: { callback?: () => Promise<Response> } = {}): Promise<Response | null> {
    const { callback } = opts
    const callbackPromise = callback ? callback() : Promise.resolve()
    const [resp] = await Promise.all([
      callbackPromise,
      this.clickByRole('button', 'Submit')
    ])
    return typeof resp === 'object' ? resp : null
  }

  async progress(): Promise<Locator> {
    return await this.formProgress
  }

  /**
   * Select either "I agree" and "I do not agree" radiobutton
   * @returns {Promise<this>}
   * @param yes True to click "I agree" radiobutton.
   */
  async agree(yes = true): Promise<this> {
    const label = yes ? 'I agree' : 'I do not agree'
    await this.page.getByText(label, { exact: true }).click()
    return this
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
    return this.page.getByLabel('progress')
  }

  protected get formActionbar(): Locator {
    return this.page.locator('.sd-action-bar.sd-footer')
  }

  divFor(header: string): Locator {
    return this.page.locator(`//div[./h2[contains(normalize-space(), "${header}")]]`)
  }
}
